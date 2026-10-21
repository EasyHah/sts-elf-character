"""Generate placeholder art for the cursed (dark) face of elf cards.

Reads the existing light-face PNGs and writes NEW files only:
  images/character/cardback_cursed/<same names>   card frames + energy orbs
  images/cards/<type>/<name>_cursed[_p].png       tinted card art placeholders
  art-review/cursed_preview.png                   side-by-side review sheet

Usage: python tools/make_cursed_art.py
"""
import colorsys
import os
import sys

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

PROJECT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
IMAGES = os.path.join(PROJECT, "src", "main", "resources", "elfprototype", "images")
REVIEW = os.path.join(PROJECT, "art-review")

# Dark elf palette (from optional/duality/designs/dark): violet hood, silver blades.
GREEN_HUE_RANGE = (70 / 360, 180 / 360)
GOLD_HUE_RANGE = (15 / 360, 70 / 360)
VIOLET_HUE = 272 / 360
LAVENDER_HUE = 300 / 360


def remap(arr, value_scale):
    """Map greens to violet and golds to pale lavender; keep alpha untouched."""
    rgb = arr[..., :3].astype(np.float32) / 255.0
    r, g, b = rgb[..., 0], rgb[..., 1], rgb[..., 2]
    hsv = np.vectorize(colorsys.rgb_to_hsv, otypes=[np.float32] * 3)(r, g, b)
    h, s, v = hsv

    green = (h >= GREEN_HUE_RANGE[0]) & (h < GREEN_HUE_RANGE[1]) & (s > 0.12)
    gold = (h >= GOLD_HUE_RANGE[0]) & (h < GOLD_HUE_RANGE[1]) & (s > 0.15)

    h = np.where(green, VIOLET_HUE, h)
    s = np.where(green, np.clip(s * 1.05, 0, 1), s)
    v = np.where(green, v * value_scale, v)

    h = np.where(gold, LAVENDER_HUE, h)
    s = np.where(gold, s * 0.45, s)
    v = np.where(gold, v * 0.92, v)

    out = np.vectorize(colorsys.hsv_to_rgb, otypes=[np.float32] * 3)(h, s, v)
    res = arr.copy()
    res[..., :3] = np.clip(np.stack(out, axis=-1) * 255.0 + 0.5, 0, 255).astype(np.uint8)
    return res


def cursed_frame(src, dst):
    im = Image.open(src).convert("RGBA")
    out = Image.fromarray(remap(np.array(im), value_scale=1.05), "RGBA")
    out.save(dst)
    return im, out


def cursed_card_art(src, dst):
    """Recolor, darken, and add a violet vignette. Placeholder only."""
    im = Image.open(src).convert("RGBA")
    arr = remap(np.array(im), value_scale=0.75)
    rgb = arr[..., :3].astype(np.float32)

    # Push the whole image toward a cold violet and lower brightness.
    tint = np.array([70, 40, 110], dtype=np.float32)
    rgb = rgb * 0.72 + tint * 0.28

    # Vignette: dark violet edges, strength grows toward the border.
    h, w = rgb.shape[:2]
    mask = Image.new("L", (w, h), 0)
    ImageDraw.Draw(mask).ellipse([w * 0.08, h * 0.05, w * 0.92, h * 0.95], fill=255)
    mask = mask.filter(ImageFilter.GaussianBlur(radius=min(w, h) * 0.12))
    keep = np.array(mask, dtype=np.float32)[..., None] / 255.0
    edge = np.array([25, 10, 40], dtype=np.float32)
    rgb = rgb * keep + edge * (1.0 - keep) * 0.9 + rgb * (1.0 - keep) * 0.1

    arr[..., :3] = np.clip(rgb + 0.5, 0, 255).astype(np.uint8)
    out = Image.fromarray(arr, "RGBA")
    out.save(dst)
    return im, out


def checker(size, cell=16):
    w, h = size
    bg = Image.new("RGBA", size, (200, 200, 200, 255))
    d = ImageDraw.Draw(bg)
    for y in range(0, h, cell):
        for x in range(0, w, cell):
            if (x // cell + y // cell) % 2:
                d.rectangle([x, y, x + cell - 1, y + cell - 1], fill=(235, 235, 235, 255))
    return bg


def review_sheet(pairs, dst, thumb=220):
    """pairs: list of (label, light_img, cursed_img). Two columns per row."""
    pad, label_h = 12, 18
    cols = 2
    rows = (len(pairs) + cols - 1) // cols
    cell_w = thumb * 2 + pad * 3
    cell_h = thumb + label_h + pad * 2
    sheet = Image.new("RGBA", (cell_w * cols, cell_h * rows), (40, 40, 40, 255))
    draw = ImageDraw.Draw(sheet)
    for i, (label, a, b) in enumerate(pairs):
        ox, oy = (i % cols) * cell_w, (i // cols) * cell_h
        draw.text((ox + pad, oy + 4), label, fill=(230, 230, 230, 255))
        for j, img in enumerate((a, b)):
            t = img.copy()
            t.thumbnail((thumb, thumb))
            bg = checker(t.size)
            bg.alpha_composite(t)
            sheet.paste(bg, (ox + pad + j * (thumb + pad), oy + label_h + pad))
    sheet.convert("RGB").save(dst)


def main():
    out_frames = os.path.join(IMAGES, "character", "cardback_cursed")
    os.makedirs(out_frames, exist_ok=True)
    os.makedirs(REVIEW, exist_ok=True)

    pairs = []
    src_frames = os.path.join(IMAGES, "character", "cardback")
    for name in sorted(os.listdir(src_frames)):
        if not name.endswith(".png"):
            continue
        a, b = cursed_frame(os.path.join(src_frames, name), os.path.join(out_frames, name))
        if not name.endswith("_p.png") and name != "small_orb.png":
            pairs.append(("frame " + name, a, b))

    for ctype in ("attack", "skill", "power"):
        folder = os.path.join(IMAGES, "cards", ctype)
        for name in sorted(os.listdir(folder)):
            if not name.endswith(".png") or "_cursed" in name:
                continue
            stem, ext = os.path.splitext(name)
            if stem.endswith("_p"):
                dst = stem[:-2] + "_cursed_p" + ext
            else:
                dst = stem + "_cursed" + ext
            a, b = cursed_card_art(os.path.join(folder, name), os.path.join(folder, dst))
            if stem.endswith("_p") and not stem.startswith("default"):
                pairs.append(("card " + ctype + "/" + stem[:-2], a, b))

    review_sheet(pairs, os.path.join(REVIEW, "cursed_preview.png"))
    print("frames ->", os.path.relpath(out_frames, PROJECT))
    print("review ->", os.path.relpath(os.path.join(REVIEW, "cursed_preview.png"), PROJECT))
    return 0


if __name__ == "__main__":
    sys.exit(main())
