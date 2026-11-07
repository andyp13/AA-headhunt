#!/usr/bin/env bash
# Build the Pi 5 Android Automotive image. First build: several hours.
set -euo pipefail
# shellcheck source=tools/rpi5/env.sh
source "$(dirname "$0")/env.sh"
cd "$HH_AOSP_ROOT"
[ -f build/envsetup.sh ] || { echo "no AOSP tree at $HH_AOSP_ROOT; run sync.sh" >&2; exit 1; }

jobs="${HH_JOBS:-$(nproc)}"
# shellcheck disable=SC1091
source build/envsetup.sh
lunch "$HH_LUNCH"
make bootimage systemimage vendorimage -j"$jobs"
./rpi5-mkimg.sh

echo
echo "images in out/target/product/rpi5/:"
for f in out/target/product/rpi5/*.img; do [ -e "$f" ] && du -h "$f"; done
echo "next: tools/rpi5/flash.sh /dev/sdX"
