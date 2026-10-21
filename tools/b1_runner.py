"""B1 runner, executed ON the remote art server (stdlib + PIL; reuses sts1_art.imaging.contain).

Reads art-b1-jobs.json next to this file and submits each candidate to the engine ComfyUI with the same
graph elf_production.py highres() used for the demo cards. Writes only under its own folder:
  <id>/cand_<seed>.png, <id>/cand_<seed>.json (prompt, workflow, seconds, peak VRAM), <id>/inputs/*.png
Existing candidates are skipped, nothing is overwritten.

Usage (remote): /workspace/ComfyUI/.venv/bin/python b1_runner.py --round 1 [--only Strike_Elf,Chime]
"""
import argparse
import io
import json
import os
import subprocess
import sys
import time
import urllib.parse
import urllib.request
import uuid

from PIL import Image

sys.path.insert(0, "/workspace/sts1-art-pipeline")
from sts1_art.imaging import contain  # same helper the demo cards were made with

HERE = os.path.dirname(os.path.abspath(__file__))
COMFY = "http://127.0.0.1:8188"


def http(method, path, data=None, headers=None, timeout=60):
    req = urllib.request.Request(COMFY + path, data=data, method=method, headers=headers or {})
    with urllib.request.urlopen(req, timeout=timeout) as r:
        return r.read()


def upload(png_bytes, name, subfolder):
    boundary = uuid.uuid4().hex
    parts = []
    for key, val in (("subfolder", subfolder), ("type", "input"), ("overwrite", "false")):
        parts.append(f'--{boundary}\r\nContent-Disposition: form-data; name="{key}"\r\n\r\n{val}\r\n'.encode())
    parts.append(f'--{boundary}\r\nContent-Disposition: form-data; name="image"; filename="{name}"\r\n'
                 f'Content-Type: image/png\r\n\r\n'.encode() + png_bytes + b"\r\n")
    parts.append(f"--{boundary}--\r\n".encode())
    up = json.loads(http("POST", "/upload/image", b"".join(parts),
                         {"Content-Type": f"multipart/form-data; boundary={boundary}"}))
    return (up.get("subfolder", "") + "/" + up["name"]).lstrip("/")


def prepare(spec, size, save_to):
    im = Image.open(spec["path"])
    im.load()
    if spec["prepare"].startswith("contain"):
        im = contain(im.convert("RGBA"), tuple(size), (255, 255, 255, 255))
    elif spec["prepare"].startswith("crop content"):
        # Source has white side bars: keep the painted area, centre-crop to the target aspect, resize.
        rgb = im.convert("RGB")
        cols = [x for x in range(rgb.width) if min(rgb.getpixel((x, y))[0] for y in range(0, rgb.height, 16)) < 235]
        x0, x1 = cols[0], cols[-1] + 1
        tw, th = size
        cw = x1 - x0
        ch = min(rgb.height, round(cw * th / tw))
        y0 = (rgb.height - ch) // 2
        im = rgb.crop((x0, y0, x1, y0 + ch)).resize(tuple(size), Image.LANCZOS)
    im.save(save_to)
    buf = io.BytesIO()
    im.save(buf, "PNG")
    return buf.getvalue()


def graph(job, wf, refs, seed, prefix):
    def node(t, **kw):
        return {"class_type": t, "inputs": kw}
    g = {"2": node("UNETLoader", unet_name=wf["unet"], weight_dtype="default"),
         "3": node("CLIPLoader", clip_name=wf["clip"][0], type=wf["clip"][1], device="default"),
         "4": node("VAELoader", vae_name=wf["vae"])}
    enc = {"clip": ["3", 0], "vae": ["4", 0], "prompt": job["prompt"], "negative_prompt": job.get("negative", wf["negative_prompt"]),
           "resolution": wf["resolution"]}
    for k, (slot, ref) in enumerate(refs.items()):
        nid = str(10 + k)
        g[nid] = node("LoadImage", image=ref)
        enc["images." + slot] = [nid, 0]
    s = wf["sampler"]
    g["5"] = node("TextEncodeQwenImage21", **enc)
    g["6"] = node("KSampler", model=["2", 0], seed=seed, steps=s["steps"], cfg=s["cfg"],
                  sampler_name=s["sampler_name"], scheduler=s["scheduler"], positive=["5", 0],
                  negative=["5", 1], latent_image=["5", 2], denoise=s["denoise"])
    g["7"] = node("VAEDecode", samples=["6", 0], vae=["4", 0])
    g["8"] = node("SaveImage", images=["7", 0], filename_prefix=prefix)
    return g


def vram():
    try:
        out = subprocess.check_output(["nvidia-smi", "--query-gpu=memory.used", "--format=csv,noheader,nounits"],
                                      text=True)
        return int(out.splitlines()[0])
    except Exception:
        return -1


def run_candidate(job, wf, refs, seed, out_dir):
    target = os.path.join(out_dir, f"cand_{seed}.png")
    if os.path.exists(target):
        print(f"  skip {job['id']} seed {seed} (exists)", flush=True)
        return
    g = graph(job, wf, refs, seed, f"elf_b1/{job['id']}/cand_{seed}")
    sub = json.loads(http("POST", "/prompt", json.dumps({"prompt": g, "client_id": "elf_b1"}).encode(),
                          {"Content-Type": "application/json"}))
    pid = sub["prompt_id"]
    t0, peak = time.monotonic(), 0
    while True:
        peak = max(peak, vram())
        hist = json.loads(http("GET", "/history/" + pid)).get(pid)
        if hist:
            status = hist.get("status", {})
            if status.get("status_str") == "error":
                raise RuntimeError(f"{job['id']} seed {seed}: " + json.dumps(status)[-1500:])
            imgs = hist.get("outputs", {}).get("8", {}).get("images", [])
            if imgs:
                raw = http("GET", "/view?" + urllib.parse.urlencode(imgs[0]), timeout=120)
                Image.open(io.BytesIO(raw)).save(target)
                with open(target[:-4] + ".json", "w", encoding="utf-8") as f:
                    json.dump({"id": job["id"], "seed": seed, "prompt": job["prompt"], "inputs": refs,
                               "seconds": round(time.monotonic() - t0, 1), "peak_vram_mib": peak,
                               "workflow_api": g}, f, ensure_ascii=False, indent=2)
                print(f"  done {job['id']} seed {seed} in {time.monotonic() - t0:.0f}s, peak {peak} MiB", flush=True)
                return
        time.sleep(2)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--round", type=int, required=True)
    ap.add_argument("--only", default="")
    ap.add_argument("--jobs", default="art-b1-jobs.json")
    args = ap.parse_args()
    doc = json.load(open(os.path.join(HERE, args.jobs), encoding="utf-8"))
    wf = doc["workflow"]
    only = set(filter(None, args.only.split(",")))
    jobs = [j for j in doc["jobs"] if j["round"] == args.round and (not only or j["id"] in only)]
    print(f"round {args.round}: {len(jobs)} jobs x candidates", flush=True)
    for job in jobs:
        out_dir = os.path.join(HERE, job["id"])
        os.makedirs(os.path.join(out_dir, "inputs"), exist_ok=True)
        refs = {}
        for slot, spec in job["images"].items():
            if not os.path.exists(spec["path"]):
                raise SystemExit(f"{job['id']}: missing input {spec['path']} (choose the light face first?)")
            data = prepare(spec, job["size"], os.path.join(out_dir, "inputs", slot + ".png"))
            refs[slot] = upload(data, f"{slot}.png", f"elf_b1/{job['id']}")
        print(f"{job['id']}: inputs {refs}", flush=True)
        for seed in job["seeds"]:
            run_candidate(job, wf, refs, seed, out_dir)
    print("round finished", flush=True)


if __name__ == "__main__":
    main()
