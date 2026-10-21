"""Read-only capability probe for the remote ComfyUI (GET requests only, submits nothing).

Answers the B1 question: can this server edit an existing image (image-to-image / instruction edit),
and is a matting node (BiRefNet / RMBG) available?

Usage: python tools/probe_comfy.py http://127.0.0.1:8188 [--out art-review/comfy_probe.json]
"""
import json
import os
import re
import sys
import urllib.error
import urllib.request

# Node class names (or fragments) that matter for B1.
EDIT_NODES = ["TextEncodeQwenImageEdit", "TextEncodeQwenImageEditPlus", "QwenImageEdit", "ReferenceLatent",
              "FluxKontext", "InpaintModelConditioning", "VAEEncodeForInpaint", "ImageStitch"]
IMG2IMG_NODES = ["LoadImage", "VAEEncode", "KSampler", "KSamplerAdvanced", "ImageScale"]
MATTING_HINTS = re.compile(r"birefnet|rmbg|removebg|remove_bg|background", re.I)
MODEL_HINTS = re.compile(r"qwen|edit|kontext|flux|birefnet|rmbg", re.I)
LOADERS = ["UNETLoader", "CheckpointLoaderSimple", "CLIPLoader", "DualCLIPLoader", "VAELoader", "LoraLoader",
           "LoraLoaderModelOnly"]


def get(base, path):
    with urllib.request.urlopen(base.rstrip("/") + path, timeout=30) as r:
        return json.loads(r.read().decode("utf-8"))


def loader_options(info, node):
    """Model file choices a loader node offers (first list-typed required input)."""
    spec = info.get(node, {}).get("input", {}).get("required", {})
    out = []
    for name, val in spec.items():
        if isinstance(val, list) and val and isinstance(val[0], list):
            out += [(name, v) for v in val[0]]
    return out


def main(base, out_path):
    report = {"base": base}
    try:
        report["system_stats"] = get(base, "/system_stats")
    except (urllib.error.URLError, OSError) as e:
        print("cannot reach", base, "->", e)
        return 1
    info = get(base, "/object_info")
    names = sorted(info)
    report["node_count"] = len(names)
    report["edit_nodes"] = {n: any(n.lower() in k.lower() for k in names) for n in EDIT_NODES}
    report["img2img_nodes"] = {n: n in info for n in IMG2IMG_NODES}
    report["matting_nodes"] = [k for k in names if MATTING_HINTS.search(k)]
    report["models"] = {n: [f"{a}={v}" for a, v in loader_options(info, n) if MODEL_HINTS.search(str(v))]
                        for n in LOADERS if n in info}
    report["qwen_related_nodes"] = [k for k in names if "qwen" in k.lower()]

    can_edit = any(report["edit_nodes"].values()) and any("edit" in m.lower()
                                                           for ms in report["models"].values() for m in ms)
    can_img2img = all(report["img2img_nodes"][n] for n in ("LoadImage", "VAEEncode", "KSampler"))
    report["verdict"] = {
        "instruction_edit": can_edit,        # plan A: edit model keeps composition by instruction
        "img2img_denoise": can_img2img,      # plan A': VAEEncode + KSampler denoise < 1
        "matting": bool(report["matting_nodes"]),
    }

    os.makedirs(os.path.dirname(out_path) or ".", exist_ok=True)
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(report, f, ensure_ascii=False, indent=2)
    print(json.dumps(report["verdict"], indent=2))
    print("edit nodes:", [k for k, v in report["edit_nodes"].items() if v])
    print("qwen nodes:", report["qwen_related_nodes"])
    print("matting nodes:", report["matting_nodes"])
    for n, ms in report["models"].items():
        if ms:
            print(n, ms)
    print("full report ->", out_path)
    return 0


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(2)
    out = "art-review/comfy_probe.json"
    if "--out" in sys.argv:
        out = sys.argv[sys.argv.index("--out") + 1]
    sys.exit(main(sys.argv[1], out))
