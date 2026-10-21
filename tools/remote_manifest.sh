#!/usr/bin/env bash
# Read-only file manifest of a remote instance, streamed to stdout (nothing is written on the server).
# Run once on the OLD instance and once on a FRESH instance made from the same paid base image:
#   ssh ... 'bash -s' < tools/remote_manifest.sh | gzip > migration/old.manifest.gz
# Line format (tab separated): type  size  mtime  mode  path  [symlink target]
set -u
find / -xdev \
  \( -path /proc -o -path /sys -o -path /dev -o -path /run -o -path /tmp -o -path /var/tmp \
     -o -path /var/cache -o -path /var/log -o -path /root/.cache -o -name __pycache__ \) -prune \
  -o \( -type f -o -type l -o -type d \) -printf '%y\t%s\t%T@\t%m\t%p\t%l\n' 2>/dev/null
