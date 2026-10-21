"""Build review sheets for downloaded B1 candidates.

For each job folder: one row of candidates (numbered 1-4), cursed faces get their image_1 source on the
left, cards add 250x190 game-size thumbnails, icons add 64 / 32 px thumbnails on grey and dark backgrounds.

Usage: python tools/b1_review.py D:/StSDev/art-inbox/B1/round1 <out_dir>
"""
import glob
import json
import os
import sys

from PIL import Image, ImageDraw

ICON_IDS = {"CrackedSeal", "Chime", "Chime__leaf"}


def card_thumb(im):
    """Cover-crop to the 250:190 card window, like the import step will."""
    w, h = im.size
    tw = int(h * 250 / 190)
    if tw <= w:
        box = ((w - tw) // 2, 0, (w - tw) // 2 + tw, h)
    else:
        th = int(w * 190 / 250)
        box = (0, (h - th) // 2, w, (h - th) // 2 + th)
    return im.crop(box).resize((250, 190), Image.LANCZOS)


def label(draw, xy, text, color=(255, 230, 120)):
    draw.rectangle([xy[0], xy[1], xy[0] + 8 * len(text) + 8, xy[1] + 18], fill=(0, 0, 0))
    draw.text((xy[0] + 4, xy[1] + 3), text, fill=color)


def sheet_for(folder, out_dir):
    job_id = os.path.basename(folder)
    cands = sorted(glob.glob(os.path.join(folder, "cand_*.png")))
    if not cands:
        return None
    ims = [Image.open(c).convert("RGB") for c in cands]
    src = os.path.join(folder, "inputs", "image_1.png")
    cursed = "_cursed" in job_id and os.path.exists(src)
    icon = job_id in ICON_IDS
    big_w = 400
    big_h = int(big_w * ims[0].height / ims[0].width)
    cols = len(ims) + (1 if cursed else 0)
    thumb_h = 200 if not icon else 110
    W, H = cols * (big_w + 10) + 10, 30 + big_h + 10 + thumb_h + 10
    sheet = Image.new("RGB", (W, H), (40, 40, 40))
    d = ImageDraw.Draw(sheet)
    d.text((10, 8), job_id, fill=(255, 255, 255))
    x = 10
    tiles = []
    if cursed:
        tiles.append(("source (light face)", Image.open(src).convert("RGB")))
    for i, im in enumerate(ims):
        seed = os.path.basename(cands[i])[5:-4]
        tiles.append((f"#{i + 1}  seed {seed}", im))
    for name, im in tiles:
        sheet.paste(im.resize((big_w, big_h), Image.LANCZOS), (x, 30))
        label(d, (x + 4, 34), name)
        ty = 30 + big_h + 10
        if icon:
            for k, (sz, bg) in enumerate(((64, (128, 128, 128)), (32, (128, 128, 128)), (64, (20, 20, 30)), (32, (20, 20, 30)))):
                t = Image.new("RGB", (sz, sz), bg)
                t.paste(im.resize((sz, sz), Image.LANCZOS))
                sheet.paste(t, (x + k * 80, ty))
        else:
            sheet.paste(card_thumb(im), (x, ty))
        x += big_w + 10
    path = os.path.join(out_dir, f"review_{job_id}.png")
    sheet.save(path)
    return path


def main(root, out_dir):
    os.makedirs(out_dir, exist_ok=True)
    made = []
    for folder in sorted(glob.glob(os.path.join(root, "*"))):
        if os.path.isdir(folder):
            p = sheet_for(folder, out_dir)
            if p:
                made.append(p)
    for p in made:
        print(p)


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print(__doc__)
        sys.exit(2)
    main(sys.argv[1], sys.argv[2])
