"""Remote: cut chosen relic / icon images to transparent PNG with BiRefNet (same node chain as the existing
birefnet.json workflow: RemoveBackground -> InvertMask -> JoinImageWithAlpha). Runs both birefnet models so the
softer matting model can be compared on glowing edges. Writes <id>/matte_<model>.png, never overwrites.

Usage (remote): /workspace/ComfyUI/.venv/bin/python b1_matte.py CrackedSeal Chime
"""
import io
import json
import os
import sys
import time
import urllib.parse

from PIL import Image

from b1_runner import http, upload

HERE = os.path.dirname(os.path.abspath(__file__))
MODELS = os.environ.get("MATTE_MODELS", "birefnet.safetensors,birefnet-matting.safetensors").split(",")


def matte(job_id, model):
    out = os.path.join(HERE, job_id, "matte_" + model.split(".")[0] + ".png")
    if os.path.exists(out):
        print("skip", out)
        return
    with open(os.path.join(HERE, job_id, "chosen.png"), "rb") as f:
        ref = upload(f.read(), "chosen.png", f"elf_b1/{job_id}")

    def node(t, **kw):
        return {"class_type": t, "inputs": kw}
    g = {"1": node("LoadImage", image=ref),
         "2": node("LoadBackgroundRemovalModel", bg_removal_name=model),
         "3": node("RemoveBackground", bg_removal_model=["2", 0], image=["1", 0]),
         "4": node("InvertMask", mask=["3", 0]),
         "5": node("JoinImageWithAlpha", image=["1", 0], alpha=["4", 0]),
         "6": node("SaveImage", images=["5", 0], filename_prefix=f"elf_b1/{job_id}/matte")}
    pid = json.loads(http("POST", "/prompt", json.dumps({"prompt": g, "client_id": "elf_b1"}).encode(),
                          {"Content-Type": "application/json"}))["prompt_id"]
    while True:
        hist = json.loads(http("GET", "/history/" + pid)).get(pid)
        if hist:
            if hist.get("status", {}).get("status_str") == "error":
                raise RuntimeError(json.dumps(hist["status"])[-1500:])
            imgs = hist.get("outputs", {}).get("6", {}).get("images", [])
            if imgs:
                raw = http("GET", "/view?" + urllib.parse.urlencode(imgs[0]), timeout=120)
                Image.open(io.BytesIO(raw)).save(out)
                print("done", out)
                return
        time.sleep(1)


if __name__ == "__main__":
    for jid in sys.argv[1:]:
        for m in MODELS:
            matte(jid, m)
