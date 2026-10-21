"""Import chosen remote art into src/main/resources (cards, relics, power icons).

Sizes follow the files already in the mod:
  cards   1024x768 -> centre cover-crop to 250:190 -> <ID>_p.png 500x380 and <ID>.png 250x190 (opaque)
  relic   transparent master -> object ~50% of canvas -> <ID>.png 128, large/<ID>.png 256,
          <ID>Outline.png 128 = white silhouette dilated 2 px (like WorldtreeBlessingOutline.png)
  power   transparent master -> object ~87% of canvas -> large/<ID>.png 84, <ID>.png 32
Any file that would be overwritten is first copied to art-review/replaced/<timestamp>/.
Writes art-review/<batch>_import.json (sources, outputs, sha256) and a contact sheet.

Usage: python tools/import_art.py D:/StSDev/art-inbox/B1/final B1
       python tools/import_art.py <root> B2-B5 --jobs docs/art-pipeline/art-all-jobs.json
"""
import hashlib
import json
import os
import shutil
import sys
import time

from PIL import Image, ImageDraw, ImageFilter

PROJECT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
IMAGES = os.path.join(PROJECT, "src", "main", "resources", "elfprototype", "images")
REVIEW = os.path.join(PROJECT, "art-review")

# id -> (kind, card type). Cursed faces share the type of their light face.
B1 = {
    "Strike_Elf": ("card", "attack"), "Strike_Elf_cursed": ("card", "attack"),
    "Defend_Elf": ("card", "skill"), "Defend_Elf_cursed": ("card", "skill"),
    "CurseWhisper": ("card", "skill"), "CurseWhisper_cursed": ("card", "skill"),
    "SingleEdge": ("card", "attack"), "SingleEdge_cursed": ("card", "attack"),
    "ChargedShot_cursed": ("card", "attack"), "LeafWard_cursed": ("card", "skill"),
    "CrackedSeal": ("relic", None), "Chime": ("power", None),
}
BATCHES = {"B1": B1}


def sha(path):
    return hashlib.sha256(open(path, "rb").read()).hexdigest()


class Writer:
    def __init__(self):
        self.backup_dir = os.path.join(REVIEW, "replaced", time.strftime("%Y%m%d-%H%M%S"))
        self.outputs = []

    def save(self, im, rel):
        dst = os.path.join(IMAGES, rel)
        os.makedirs(os.path.dirname(dst), exist_ok=True)
        if os.path.exists(dst):
            keep = os.path.join(self.backup_dir, rel)
            os.makedirs(os.path.dirname(keep), exist_ok=True)
            shutil.copy2(dst, keep)
        im.save(dst)
        self.outputs.append({"file": rel.replace(os.sep, "/"), "size": list(im.size), "sha256": sha(dst)})
        return dst


def cover_card(im):
    im = im.convert("RGB")
    w, h = im.size
    tw = round(h * 250 / 190)
    if tw <= w:
        x = (w - tw) // 2
        im = im.crop((x, 0, x + tw, h))
    else:
        th = round(w * 190 / 250)
        y = (h - th) // 2
        im = im.crop((0, y, w, y + th))
    return im.resize((500, 380), Image.LANCZOS), im.resize((250, 190), Image.LANCZOS)


def fit_object(im, canvas, fraction):
    im = im.convert("RGBA")
    box = im.getchannel("A").point(lambda a: 255 if a > 8 else 0).getbbox()
    obj = im.crop(box)
    if max(obj.size) / min(obj.size) > 1.8:  # flat objects (crown) read too thin at the square fraction
        fraction = min(0.72, fraction * 1.4)
    scale = canvas * fraction / max(obj.size)
    obj = obj.resize((max(1, round(obj.width * scale)), max(1, round(obj.height * scale))), Image.LANCZOS)
    out = Image.new("RGBA", (canvas, canvas), (0, 0, 0, 0))
    out.alpha_composite(obj, ((canvas - obj.width) // 2, (canvas - obj.height) // 2))
    return out


def outline(icon128):
    alpha = icon128.getchannel("A").filter(ImageFilter.MaxFilter(5))
    out = Image.new("RGBA", icon128.size, (255, 255, 255, 0))
    out.putalpha(alpha)
    return out


def contact(rows, path):
    th = 200
    tiles = []
    for name, im in rows:
        t = im.copy()
        t.thumbnail((380, th))
        bg = Image.new("RGBA", (max(t.width, 120), th + 22), (55, 55, 60, 255))
        bg.alpha_composite(t.convert("RGBA"), (0, 22))
        ImageDraw.Draw(bg).text((4, 4), name, fill=(255, 230, 120, 255))
        tiles.append(bg)
    cols = 4
    cw = max(t.width for t in tiles) + 10
    sheet = Image.new("RGB", (cols * cw + 10, ((len(tiles) + cols - 1) // cols) * (th + 32) + 10), (35, 35, 35))
    for i, t in enumerate(tiles):
        sheet.paste(t.convert("RGB"), (10 + (i % cols) * cw, 10 + (i // cols) * (th + 32)))
    sheet.save(path)


def main(src_root, batch):
    w = Writer()
    record = {"batch": batch, "source": src_root, "items": []}
    rows = []
    for job_id, (kind, ctype) in BATCHES[batch].items():
        aid = game_id(job_id)
        if kind == "card":
            src = os.path.join(src_root, job_id, "chosen.png")
            big, small = cover_card(Image.open(src))
            w.save(big, os.path.join("cards", ctype, aid + "_p.png"))
            w.save(small, os.path.join("cards", ctype, aid + ".png"))
            rows.append((aid, small))
        else:
            src = os.path.join(src_root, job_id, "matte_birefnet.png")
            master = Image.open(src)
            if kind == "relic":
                r128, r256 = fit_object(master, 128, 0.5), fit_object(master, 256, 0.5)
                w.save(r128, os.path.join("relics", aid + ".png"))
                w.save(r256, os.path.join("relics", "large", aid + ".png"))
                w.save(outline(r128), os.path.join("relics", aid + "Outline.png"))
                rows += [(aid + " 128", r128), (aid + " outline", outline(r128))]
            else:
                p84, p32 = fit_object(master, 84, 0.87), fit_object(master, 32, 0.87)
                w.save(p84, os.path.join("powers", "large", aid + ".png"))
                w.save(p32, os.path.join("powers", aid + ".png"))
                rows += [(aid + " 84", p84), (aid + " 32", p32)]
        record["items"].append({"id": aid, "kind": kind, "source": os.path.relpath(src, src_root).replace(os.sep, "/"),
                                "source_sha256": sha(src)})
    record["outputs"] = w.outputs
    record["replaced_backup"] = os.path.relpath(w.backup_dir, PROJECT) if os.path.isdir(w.backup_dir) else None
    with open(os.path.join(REVIEW, f"{batch.lower()}_import.json"), "w", encoding="utf-8") as f:
        json.dump(record, f, ensure_ascii=False, indent=2)
    contact(rows, os.path.join(REVIEW, f"{batch.lower()}_import.png"))
    print(len(w.outputs), "files written;", "backup:", record["replaced_backup"])


def batch_from_jobs(jobs_path):
    """Derive {asset id: (kind, card type)} from a job file. Job ids carry suffixes the game files do not:
    power-card art is '<Power>_card', power icons '<Power>_icon'; the folder name stays the job id."""
    items = {}
    for j in json.load(open(jobs_path, encoding="utf-8"))["jobs"]:
        if j["kind"] == "card":
            items[j["id"]] = ("card", j["card_type"])
        elif j["kind"] == "power_icon":
            items[j["id"]] = ("power", None)
        else:
            items[j["id"]] = ("relic", None)
    return items


def game_id(job_id):
    for suffix in ("_card", "_icon"):
        if job_id.endswith(suffix):
            return job_id[:-len(suffix)]
    return job_id


if __name__ == "__main__":
    if len(sys.argv) == 5 and sys.argv[3] == "--jobs":
        BATCHES[sys.argv[2]] = batch_from_jobs(sys.argv[4])
    elif len(sys.argv) != 3:
        print(__doc__)
        sys.exit(2)
    main(sys.argv[1], sys.argv[2])
