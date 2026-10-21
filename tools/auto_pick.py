"""Pre-rank downloaded candidates and draw overview sheets for a quick visual pass.

Scores (higher is better), all heuristics; the final choice is made by looking at the sheets:
  cards   - blank border penalty (B1 problem: white / black empty backgrounds), detail (edge energy)
  cursed  - composition similarity to the chosen light face (edge-map correlation), too-dark penalty
  icons   - object size 15-60% of canvas, centred, a single main object
Writes <out>/picks.json ({id: {"seed", "scores"}}) and <out>/sheet_NN.png (6 jobs per sheet, pick framed).
Manual overrides: <out>/overrides.json {id: seed} is applied on top if present.

Usage: python tools/auto_pick.py <downloaded root> <jobs json> <round> <out dir>
"""
import glob
import json
import os
import sys

import numpy as np
from PIL import Image, ImageDraw


def gray(im, size):
    return np.asarray(im.convert("L").resize(size, Image.BILINEAR), dtype=np.float32) / 255.0


def edges(g):
    gx = np.abs(np.diff(g, axis=1))[:-1, :]
    gy = np.abs(np.diff(g, axis=0))[:, :-1]
    return gx + gy


def blank_border(im, count_black=True):
    a = np.asarray(im.convert("RGB").resize((256, 192)), dtype=np.int16)
    b = 10
    strip = np.concatenate([a[:b].reshape(-1, 3), a[-b:].reshape(-1, 3), a[:, :b].reshape(-1, 3), a[:, -b:].reshape(-1, 3)])
    white = (strip.min(axis=1) > 232).mean()
    black = (strip.max(axis=1) < 16).mean()
    return float(max(white, black)) if count_black else float(white)


def score_card(im, count_black=True):
    bb = blank_border(im, count_black)
    detail = float(edges(gray(im, (256, 192))).mean())
    return {"blank_border": round(bb, 3), "detail": round(detail, 4), "score": round(detail * 10 - bb * 3, 4)}


def score_cursed(im, source):
    base = score_card(im, count_black=False)  # cursed faces are dark by design; only white borders are blank
    e1, e2 = edges(gray(im, (128, 96))).ravel(), edges(gray(source, (128, 96))).ravel()
    sim = float(np.corrcoef(e1, e2)[0, 1])
    lum = float(gray(im, (64, 48)).mean())
    dark = max(0.0, 0.10 - lum) * 10
    base.update({"similarity": round(sim, 3), "luminance": round(lum, 3),
                 "score": round(sim * 3 + base["detail"] * 5 - base["blank_border"] * 3 - dark, 4)})
    return base


def score_icon(im):
    a = np.asarray(im.convert("RGB").resize((256, 256)), dtype=np.int16)
    mask = a.min(axis=2) < 225
    area = float(mask.mean())
    if not mask.any():
        return {"area": 0, "score": -9}
    ys, xs = np.nonzero(mask)
    cx, cy = xs.mean() / 256 - 0.5, ys.mean() / 256 - 0.5
    off = float(np.hypot(cx, cy))
    # crude "single object": share of mask pixels inside the main bbox of the densest 90% region
    x0, x1 = np.percentile(xs, [2, 98])
    y0, y1 = np.percentile(ys, [2, 98])
    fill = float(mask[int(y0):int(y1) + 1, int(x0):int(x1) + 1].mean())
    size_pen = 0 if 0.15 <= area <= 0.6 else min(abs(area - 0.15), abs(area - 0.6)) * 5
    return {"area": round(area, 3), "offcentre": round(off, 3), "fill": round(fill, 3),
            "score": round(fill * 2 - off * 4 - size_pen, 4)}


def main(root, jobs_path, rnd, out):
    os.makedirs(out, exist_ok=True)
    jobs = [j for j in json.load(open(jobs_path, encoding="utf-8"))["jobs"] if j["round"] == int(rnd)]
    overrides = {}
    if os.path.exists(os.path.join(out, "overrides.json")):
        overrides = json.load(open(os.path.join(out, "overrides.json"), encoding="utf-8"))
    picks, rows = {}, []
    for j in jobs:
        folder = os.path.join(root, j["id"])
        cands = []
        for seed in j["seeds"]:
            p = os.path.join(folder, f"cand_{seed}.png")
            if not os.path.exists(p):
                continue
            im = Image.open(p)
            if j["kind"] in ("power_icon", "relic"):
                s = score_icon(im)
            elif j.get("depends_on"):
                src = os.path.join(folder, "inputs", "image_1.png")
                s = score_cursed(im, Image.open(src)) if os.path.exists(src) else score_card(im)
            else:
                s = score_card(im)
            cands.append((seed, s, im))
        if not cands:
            continue
        best = max(cands, key=lambda c: c[1]["score"])[0]
        chosen = int(overrides.get(j["id"], best))
        best_blank = next(c[1] for c in cands if c[0] == best).get("blank_border", 0)
        picks[j["id"]] = {"seed": chosen, "auto": best, "overridden": chosen != best,
                          "needs_regen": j["kind"] == "card" and best_blank > 0.5 and j["id"] not in overrides,
                          "scores": {str(s): sc for s, sc, _ in cands}}
        rows.append((j, cands, chosen))
    json.dump(picks, open(os.path.join(out, "picks.json"), "w", encoding="utf-8"), ensure_ascii=False, indent=2)

    tw, th = 300, 225
    per = 6
    for n in range(0, len(rows), per):
        chunk = rows[n:n + per]
        sheet = Image.new("RGB", (10 + 5 * (tw + 8), 10 + len(chunk) * (th + 26)), (32, 32, 36))
        d = ImageDraw.Draw(sheet)
        for r, (j, cands, chosen) in enumerate(chunk):
            y = 10 + r * (th + 26)
            d.text((10, y), j["id"], fill=(255, 255, 255))
            col = 0
            src = os.path.join(root, j["id"], "inputs", "image_1.png")
            if j.get("depends_on") and os.path.exists(src):
                t = Image.open(src).convert("RGB")
                t.thumbnail((tw, th))
                sheet.paste(t, (10, y + 16))
                d.text((14, y + 18), "source", fill=(160, 220, 255))
                col = 1
            for k, (seed, s, im) in enumerate(cands):
                t = im.convert("RGB")
                t.thumbnail((tw, th))
                x = 10 + (col + k) * (tw + 8)
                sheet.paste(t, (x, y + 16))
                if seed == chosen:
                    d.rectangle([x - 3, y + 13, x + t.width + 2, y + 16 + t.height + 2], outline=(255, 210, 0), width=4)
                d.text((x + 4, y + 18), f"#{k + 1} {s['score']}", fill=(255, 230, 120))
        sheet.save(os.path.join(out, f"sheet_{n // per + 1:02d}.png"))
    regen = [k for k, v in picks.items() if v["needs_regen"]]
    print("needs regen (blank background in every candidate):", regen)
    print(len(picks), "jobs picked;", sum(p["overridden"] for p in picks.values()), "overridden;",
          (len(rows) + per - 1) // per, "sheets ->", out)


if __name__ == "__main__":
    if len(sys.argv) != 5:
        print(__doc__)
        sys.exit(2)
    main(*sys.argv[1:])
