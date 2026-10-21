"""Offline asset sanity check: per action, where the lowest opaque pixel row sits vs the foot pivot.
Not a game test. Usage: python -I tools/check_pivot.py <resources_dir>"""
import json, sys, os
from PIL import Image
root = os.path.join(sys.argv[1], "elfprototype/images/character/animations/elf")
for act in ["idle", "attack", "cast", "guard", "hit", "death", "victory"]:
    meta = json.load(open(os.path.join(root, act, "animation.json"), encoding="utf-8"))
    py = meta["pivot"]["xy"][1]; px = meta["pivot"]["xy"][0]
    bottoms, centers = [], []
    for f in sorted(meta["frames"], key=lambda f: f["index"]):
        im = Image.open(os.path.join(root, act, f["file"]))
        assert im.mode == "RGBA" and im.size == (512, 512), (act, f["file"], im.mode, im.size)
        a = im.getchannel("A").point(lambda v: 255 if v > 32 else 0)
        bb = a.getbbox()
        bottoms.append(bb[3] - 1); centers.append((bb[0] + bb[2]) / 2)
    print(f"{act:8s} n={len(bottoms):2d} pivot=({px},{py}) lowest-opaque-row min/max={min(bottoms)}/{max(bottoms)} "
          f"bbox-center-x min/max={min(centers):.0f}/{max(centers):.0f}")
