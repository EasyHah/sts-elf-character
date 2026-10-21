"""Cut dark / intermediate clips from the optional/duality H3 candidates.

The duality rgba frames are 512x512 at the same character scale as the base
elf (standing height 319 vs 318 px), so only a translation is applied to put
the feet on the base pivot (256, 448). One transform per form, no per-frame
recentering.

Writes review material only (nothing under src/):
  art-review/dark_attack/<candidate>/attack_000.png ...
  art-review/intermediate_idle/idle_000.png ...
  art-review/dark_attack_compare.gif   all candidates side by side
  art-review/intermediate_idle.gif

With --export <candidate> it also writes the runtime clips (chosen 2026-10-20: C_snap):
  images/character/animations/dark/attack/          action attack_cursed, 576x512, 24 fps
  images/character/animations/intermediate/idle/    action idle_cursed, every other frame (36), 12 fps

Usage: python tools/cut_form_clips.py <ElfWindowsResources dir> [--export C_snap]
"""
import json
import os
import sys

import numpy as np
from PIL import Image, ImageDraw

PROJECT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ANIM_OUT = os.path.join(PROJECT, "src", "main", "resources", "elfprototype", "images",
                        "character", "animations")
REVIEW = os.path.join(PROJECT, "art-review")
BASE_IDLE = os.path.join(PROJECT, "src", "main", "resources", "elfprototype", "images",
                         "character", "animations", "elf", "idle", "frames")

# Translation onto the base pivot, measured from frame 0 (hip column, lowest foot row).
OFFSETS = {"dark": (27, 10), "intermediate": (53, 10)}
# Canvas per form; dark gets 64 px of right padding so the blade tips survive the shift.
# The pivot stays (256, 448) because padding is only added on the right.
CANVAS = {"dark": (576, 512), "intermediate": (512, 512)}

# Source frame indices (0-based). 24 fps, 12 frames = 0.5 s like the base attack.
# "hit" is the index inside the clip where both blades finish the sweep (src 47).
DARK_CANDIDATES = {
    "A_heavy": {"frames": [36, 38, 40, 41, 42, 43, 44, 45, 46, 47, 49, 52], "hit": 9,
                "note": "full pull-back, late heavy hit (0.38 s)"},
    "B_quick": {"frames": [40, 42, 43, 44, 45, 46, 47, 48, 50, 53, 56, 60], "hit": 6,
                "note": "short windup, hit at 0.25 s, ends in thrust pose"},
    "C_snap": {"frames": [42, 43, 44, 45, 46, 47, 48, 50, 52, 55, 58, 62], "hit": 5,
               "note": "overhead swing only, fastest hit (0.21 s)"},
}
INTERMEDIATE_IDLE = list(range(0, 38)) + list(range(36, 0, -1))  # ping-pong, seamless
FPS = 24


def load(root, form, i):
    path = os.path.join(root, "optional", "duality", "animations", form, "rgba",
                        "frame_%05d_.png" % (i + 1))
    return Image.open(path).convert("RGBA")


def place(img, form):
    dx, dy = OFFSETS[form]
    out = Image.new("RGBA", CANVAS[form], (0, 0, 0, 0))
    out.alpha_composite(img, (dx, dy))
    return out


def clipped_pixels(img, form):
    """Opaque pixels pushed off the 512 canvas by the translation."""
    dx, dy = OFFSETS[form]
    cw, ch = CANVAS[form]
    a = np.array(img)[..., 3] > 20
    h, w = a.shape
    return int(a[:, max(0, cw - dx):].sum() + a[max(0, ch - dy):, :].sum())


def save_clip(frames, folder, prefix):
    os.makedirs(folder, exist_ok=True)
    for k, f in enumerate(frames):
        f.save(os.path.join(folder, "%s_%03d.png" % (prefix, k)))


def on_bg(img, label=None, flash=False):
    bg = Image.new("RGBA", PANEL, (236, 236, 236, 255))
    d = ImageDraw.Draw(bg)
    d.line([(0, 448), (PANEL[0], 448)], fill=(200, 200, 200, 255))
    d.line([(256, 440), (256, 456)], fill=(220, 60, 60, 255))
    bg.alpha_composite(img)  # every clip shares pivot (256, 448), so top-left alignment is correct
    d = ImageDraw.Draw(bg)
    if label:
        d.text((10, 10), label, fill=(30, 30, 30, 255))
    if flash:
        d.rectangle([0, 0, PANEL[0] - 1, PANEL[1] - 1], outline=(220, 40, 40, 255), width=6)
        d.text((10, 26), "HIT", fill=(220, 40, 40, 255))
    return bg


PANEL = (576, 512)


def padded(img):
    out = Image.new("RGBA", PANEL, (0, 0, 0, 0))
    out.alpha_composite(img)
    return out


def crossfade(a, b, steps):
    a, b = padded(a), padded(b)
    return [Image.blend(a, b, (k + 1) / (steps + 1)) for k in range(steps)]


def save_gif(frames, path, scale=0.5):
    small = [f.convert("RGB").resize((int(f.width * scale), int(f.height * scale)), Image.LANCZOS)
             for f in frames]
    small[0].save(path, save_all=True, append_images=small[1:], duration=int(1000 / FPS), loop=0,
                  optimize=True)


def write_clip(folder, prefix, frames, meta, src_indices):
    """Runtime clip in the pack's animation.json format (full frames, no atlas)."""
    if os.path.isdir(folder) and os.listdir(folder):
        raise SystemExit("refusing to overwrite existing clip: " + folder)
    save_clip(frames, os.path.join(folder, "frames"), prefix)
    meta = dict(meta)
    meta.update({
        "frame_index_base": 0,
        "frame_count": len(frames),
        "duration_seconds": len(frames) / meta["fps"],
        "pivot": {"origin": "top_left", "xy": [256, 448]},
        "alpha": "straight",
        "color_space": "sRGB",
        "source_frame_indices": src_indices,
        "frames": [{"file": "frames/%s_%03d.png" % (prefix, k), "source_index": s, "index": k}
                   for k, s in enumerate(src_indices)],
    })
    with open(os.path.join(folder, "animation.json"), "w", encoding="utf-8") as f:
        json.dump(meta, f, ensure_ascii=False, indent=2)


def export(root, candidate):
    c = DARK_CANDIDATES[candidate]
    write_clip(os.path.join(ANIM_OUT, "dark", "attack"), "attack",
               [place(load(root, "dark", i), "dark") for i in c["frames"]],
               {"asset_id": "AN-D01", "form": "dark", "action": "attack_cursed",
                "canvas": list(CANVAS["dark"]), "fps": FPS, "loop": False, "end_action": "idle",
                "events": [{"name": "visual_hit", "frame_index": c["hit"],
                            "time_seconds": c["hit"] / FPS, "scope": "visual_only"}],
                "source_layout": {"kind": "duality_h3_translated", "candidate": candidate,
                                  "source_canvas": [512, 512], "offset": list(OFFSETS["dark"])}},
               c["frames"])
    # Every other frame of the ping-pong (0..36 then 34..2): 36 frames, no doubled turnaround frame.
    idle_src = list(range(0, 38, 2)) + list(range(34, 0, -2))
    write_clip(os.path.join(ANIM_OUT, "intermediate", "idle"), "idle",
               [place(load(root, "intermediate", i), "intermediate") for i in idle_src],
               {"asset_id": "AN-I01", "form": "intermediate", "action": "idle_cursed",
                "canvas": list(CANVAS["intermediate"]), "fps": FPS // 2, "loop": True,
                "end_action": "idle_cursed", "events": [],
                "source_layout": {"kind": "duality_h3_translated", "ping_pong": True,
                                  "source_canvas": [512, 512], "offset": list(OFFSETS["intermediate"])}},
               idle_src)
    print("exported", candidate, "->", os.path.relpath(ANIM_OUT, PROJECT), "dark/attack, intermediate/idle")


def main(root):
    os.makedirs(REVIEW, exist_ok=True)

    inter_raw = [load(root, "intermediate", i) for i in INTERMEDIATE_IDLE]
    print("intermediate idle clipped px", max(clipped_pixels(r, "intermediate") for r in inter_raw))
    inter = [place(r, "intermediate") for r in inter_raw]
    save_clip(inter, os.path.join(REVIEW, "intermediate_idle"), "idle")
    save_gif([on_bg(f, "intermediate idle (ping-pong %d f)" % len(inter)) for f in inter],
             os.path.join(REVIEW, "intermediate_idle.gif"))

    base_idle = Image.open(os.path.join(BASE_IDLE, "idle_000.png")).convert("RGBA")
    report = []
    panels = {}
    for name, c in DARK_CANDIDATES.items():
        raw = [load(root, "dark", i) for i in c["frames"]]
        lost = max(clipped_pixels(r, "dark") for r in raw)
        clip = [place(r, "dark") for r in raw]
        save_clip(clip, os.path.join(REVIEW, "dark_attack", name), "attack")
        report.append("%s: src %s, hit idx %d, %s, clipped px %d"
                      % (name, c["frames"], c["hit"], c["note"], lost))

        # Simulated in-game sequence: intermediate idle -> 3-frame crossfade -> attack
        # -> 3-frame crossfade -> intermediate idle hold (the runtime will blend the same way).
        seq = inter[:10]
        seq += crossfade(inter[10], clip[0], 3)
        seq += clip
        seq += crossfade(clip[-1], inter[0], 3)
        seq += [inter[0]] * 10
        hit_at = 10 + 3 + c["hit"]
        panels[name] = [on_bg(f, name, flash=(k in (hit_at, hit_at + 1))) for k, f in enumerate(seq)]

    # Side by side, same timing, with the base elf idle as size reference on the left.
    n = max(len(p) for p in panels.values())
    ref = on_bg(base_idle, "base elf (size ref)")
    combo = []
    for k in range(n):
        row = Image.new("RGBA", (PANEL[0] * (len(panels) + 1), PANEL[1]))
        row.paste(ref, (0, 0))
        for j, p in enumerate(panels.values()):
            row.paste(p[min(k, len(p) - 1)], (PANEL[0] * (j + 1), 0))
        combo.append(row)
    save_gif(combo, os.path.join(REVIEW, "dark_attack_compare.gif"), scale=0.45)

    with open(os.path.join(REVIEW, "dark_attack_candidates.txt"), "w", encoding="utf-8") as f:
        f.write("\n".join(report) + "\n")
    print("\n".join(report))
    return 0


if __name__ == "__main__":
    if len(sys.argv) == 4 and sys.argv[2] == "--export":
        export(sys.argv[1], sys.argv[3])
        sys.exit(0)
    if len(sys.argv) != 2:
        print(__doc__)
        sys.exit(2)
    sys.exit(main(sys.argv[1]))
