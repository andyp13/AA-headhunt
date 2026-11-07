#!/usr/bin/env bash
# Prepare an Ubuntu 22.04 build host for the AOSP + raspberry-vanilla build.
# Idempotent. Run once, then sync.sh. Written from the AOSP "Establish a build
# environment" page and the raspberry-vanilla README; see platform/rpi5/README.md.
set -euo pipefail

echo "== host check"
cores=$(nproc); mem_gb=$(awk '/MemTotal/ {printf "%d", $2/1024/1024}' /proc/meminfo)
free_gb=$(df -BG --output=avail "${HH_AOSP_ROOT:-$HOME}" 2>/dev/null | tail -1 | tr -dc '0-9')
echo "cores=$cores ram=${mem_gb}G free=${free_gb:-?}G"
[ "$mem_gb" -ge 32 ] || echo "warning: AOSP wants 64 GB RAM; 32 GB works with swap and -j$((cores/2))" >&2
[ "${free_gb:-0}" -ge 400 ] || echo "warning: a full checkout plus out/ is ~400 GB" >&2

echo "== packages"
sudo apt-get update -qq
# AOSP build dependencies (source.android.com/docs/setup/start/requirements)
sudo apt-get install -y git-core gnupg flex bison build-essential zip curl zlib1g-dev \
  libc6-dev-i386 x11proto-core-dev libx11-dev lib32z1-dev libgl1-mesa-dev libxml2-utils \
  xsltproc unzip fontconfig python3 python-is-python3
# raspberry-vanilla extras for rpi5-mkimg.sh
sudo apt-get install -y dosfstools e2fsprogs fdisk kpartx mtools rsync

echo "== repo tool"
mkdir -p "$HOME/bin"
if [ ! -x "$HOME/bin/repo" ]; then
  curl -sSL https://storage.googleapis.com/git-repo-downloads/repo -o "$HOME/bin/repo"
  chmod a+x "$HOME/bin/repo"
fi
# shellcheck disable=SC2016  # the PATH line is meant to be written literally into .bashrc
case ":$PATH:" in *":$HOME/bin:"*) ;; *) echo 'export PATH="$HOME/bin:$PATH"' >> "$HOME/.bashrc"; echo "added ~/bin to PATH in ~/.bashrc; open a new shell" ;; esac

echo "== git identity (repo needs one)"
git config --global user.name  >/dev/null || git config --global user.name  "Headhunt Build"
git config --global user.email >/dev/null || git config --global user.email "build@headhunt.invalid"
git config --global color.ui auto

echo "done. next: tools/rpi5/sync.sh"
