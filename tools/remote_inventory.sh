#!/usr/bin/env bash
# Read-only migration inventory of the remote art server. Changes nothing, writes nothing on the server.
# Run from Windows (Git Bash), works in the server's no-GPU mode:
#   ssh -i ~/.ssh/id_ed25519_elfart -o IdentitiesOnly=yes -p <port> root@<host> 'bash -s' < tools/remote_inventory.sh > art-review/remote_inventory.txt
set -u
W=/workspace
STUDIO=$W/WalkingwithAI-Studio-v260827

echo "## mounts (is /workspace its own disk?)"
df -h / $W 2>/dev/null
mount | grep -E " $W| / " | head

echo; echo "## size per top-level directory (GB, real files)"
du -sh --apparent-size $W/* 2>/dev/null | sort -h

echo; echo "## model files: real size and where symlinks point"
for d in $STUDIO/engine/ComfyUI/models $STUDIO/models $W/ComfyUI/models; do
  [ -d "$d" ] || continue
  echo "-- $d  real: $(du -sh "$d" 2>/dev/null | cut -f1)  following links: $(du -shL "$d" 2>/dev/null | cut -f1)"
  find "$d" -maxdepth 3 -type l -printf "   link %p -> %l\n" 2>/dev/null | head -20
done

echo; echo "## files B1 needs (Qwen Image 2.1 + BiRefNet)"
for f in diffusion_models/qwen_image_2.1_int8_convrot.safetensors text_encoders/qwen3vl_8b_int8_convrot.safetensors \
         vae/qwen_image_2.1_vae_bf16.safetensors background_removal/birefnet.safetensors \
         background_removal/birefnet-matting.safetensors; do
  p=$STUDIO/engine/ComfyUI/models/$f
  printf "   %-60s %s\n" "$f" "$(du -shL "$p" 2>/dev/null | cut -f1 || echo missing)  $(readlink -f "$p")"
done

echo; echo "## python environments"
for py in $(ls -d $STUDIO/engine/*/bin/python* $STUDIO/*/bin/python* $W/*/bin/python* /usr/local/miniconda3/envs/*/bin/python 2>/dev/null | sort -u); do
  echo "   $py  $($py --version 2>&1)  env size: $(du -sh "$(dirname "$(dirname "$py")")" 2>/dev/null | cut -f1)"
done
grep -rh -E "python|venv|conda" $STUDIO/start.py 2>/dev/null | head -5
ls -la $W/wheels 2>/dev/null | head -5; echo "   wheels: $(du -sh $W/wheels 2>/dev/null | cut -f1)"
ls $W/ComfyUI-env-backups 2>/dev/null | head

echo; echo "## GPU stack the env expects"
nvidia-smi 2>&1 | head -3
for py in $(ls -d $STUDIO/engine/*/bin/python 2>/dev/null); do
  $py -c "import torch;print('   torch',torch.__version__,'cuda',torch.version.cuda)" 2>/dev/null
done

echo; echo "## services started by the platform image"
ls /etc/supervisor/conf.d/ 2>/dev/null; grep -h -E "^\[program|command=" /etc/supervisor/conf.d/* 2>/dev/null

echo; echo "## hard-coded absolute paths (count of files mentioning /workspace)"
for d in $W/sts1-art-pipeline $W/character-pipeline-h3 $STUDIO/backend $STUDIO/.quickai; do
  [ -d "$d" ] && echo "   $d: $(grep -rIl /workspace "$d" --include=*.py --include=*.json --include=*.yaml --include=*.sh 2>/dev/null | wc -l) files"
done

echo; echo "## project data that must survive (state, records, outputs)"
du -sh $W/sts1-art-pipeline/data $W/sts1-art-pipeline/elf-production-v1 $W/character-pipeline-h3/character_studio \
       $STUDIO/data $STUDIO/engine/ComfyUI/output $STUDIO/engine/ComfyUI/input 2>/dev/null
