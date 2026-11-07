#!/usr/bin/env bash
# Check out AOSP at the pinned tag plus the raspberry-vanilla local manifests.
# Pass --shallow to use --depth=1 and drop projects the Pi doesn't need
# (saves ~100 GB; you lose git history in the AOSP projects).
set -euo pipefail
# shellcheck source=tools/rpi5/env.sh
source "$(dirname "$0")/env.sh"
export PATH="$HOME/bin:$PATH"

shallow=0; [ "${1:-}" = "--shallow" ] && shallow=1
mkdir -p "$HH_AOSP_ROOT"; cd "$HH_AOSP_ROOT"

rv="https://raw.githubusercontent.com/raspberry-vanilla/android_local_manifest/$HH_RV_BRANCH"
if [ ! -d .repo ]; then
  if [ $shallow = 1 ]; then
    repo init -u https://android.googlesource.com/platform/manifest -b "$HH_AOSP_TAG" --depth=1
  else
    repo init -u https://android.googlesource.com/platform/manifest -b "$HH_AOSP_TAG"
  fi
fi
mkdir -p .repo/local_manifests
curl -sSL -o .repo/local_manifests/manifest_brcm_rpi.xml "$rv/manifest_brcm_rpi.xml"
if [ $shallow = 1 ]; then
  curl -sSL -o .repo/local_manifests/remove_projects.xml "$rv/remove_projects.xml"
fi

repo sync -c -j"$(nproc)" --no-tags --no-clone-bundle --optimized-fetch
echo
echo "synced $HH_AOSP_TAG + raspberry-vanilla $HH_RV_BRANCH into $HH_AOSP_ROOT"
echo "next: tools/rpi5/apply-headhunt-config.sh, then tools/rpi5/build.sh"
