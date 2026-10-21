"""Compare two instance manifests (from tools/remote_manifest.sh) and list what the user changed.

  old   = the instance with the user's work
  fresh = a new instance from the same base image (target region)

Writes, next to the old manifest:
  <prefix>.added.txt / .modified.txt   paths to transfer (files and symlinks; one per line)
  <prefix>.deleted.txt                 paths present only on fresh (review before deleting anything!)
  <prefix>.summary.txt                 sizes grouped by directory, largest first

Usage: python tools/manifest_diff.py migration/old.manifest.gz migration/fresh.manifest.gz [--depth 3]
"""
import collections
import gzip
import os
import sys


def load(path):
    entries = {}
    with gzip.open(path, "rt", encoding="utf-8", errors="surrogateescape") as f:
        for line in f:
            parts = line.rstrip("\n").split("\t")
            if len(parts) < 5:
                continue
            kind, size, mtime, mode, p = parts[:5]
            target = parts[5] if len(parts) > 5 else ""
            entries[p] = (kind, int(size), mtime.split(".")[0], mode, target)
    return entries


def group(path, depth):
    parts = path.strip("/").split("/")
    return "/" + "/".join(parts[:depth])


def human(n):
    for unit in ("B", "K", "M", "G", "T"):
        if n < 1024 or unit == "T":
            return f"{n:.1f}{unit}" if unit != "B" else f"{n}B"
        n /= 1024


def main(old_path, fresh_path, depth):
    old, fresh = load(old_path), load(fresh_path)
    added, modified, deleted = [], [], []
    for p, (kind, size, mtime, mode, target) in old.items():
        if kind == "d":
            continue
        f = fresh.get(p)
        if f is None:
            added.append(p)
        elif kind == "l":
            if f[0] != "l" or f[4] != target:
                modified.append(p)
        elif f[0] != kind or f[1] != size or f[2] != mtime or f[3] != mode:
            modified.append(p)
    for p, f in fresh.items():
        if f[0] != "d" and p not in old:
            deleted.append(p)

    prefix = old_path[:-len(".manifest.gz")] if old_path.endswith(".manifest.gz") else old_path
    for name, rows in (("added", added), ("modified", modified), ("deleted", deleted)):
        with open(f"{prefix}.{name}.txt", "w", encoding="utf-8", errors="surrogateescape") as out:
            out.write("\n".join(sorted(rows)) + ("\n" if rows else ""))

    sizes = collections.Counter()
    counts = collections.Counter()
    for p in added + modified:
        g = group(p, depth)
        sizes[g] += old[p][1] if old[p][0] == "f" else 0
        counts[g] += 1
    total = sum(sizes.values())
    lines = [f"to transfer: {len(added)} added + {len(modified)} modified files, {human(total)}",
             f"only on fresh (deleted on old, review): {len(deleted)}", "", "size by directory:"]
    for g, s in sizes.most_common():
        lines.append(f"  {human(s):>8}  {counts[g]:>7} files  {g}")
    with open(f"{prefix}.summary.txt", "w", encoding="utf-8") as out:
        out.write("\n".join(lines) + "\n")
    print("\n".join(lines[:40]))
    return 0


if __name__ == "__main__":
    if len(sys.argv) < 3:
        print(__doc__)
        sys.exit(2)
    d = int(sys.argv[sys.argv.index("--depth") + 1]) if "--depth" in sys.argv else 3
    sys.exit(main(sys.argv[1], sys.argv[2], d))
