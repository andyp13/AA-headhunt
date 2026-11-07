#!/usr/bin/env bash
# Write the built image to a microSD card. Destructive: asks you to type the
# device name back before it touches anything.
set -euo pipefail
# shellcheck source=tools/rpi5/env.sh
source "$(dirname "$0")/env.sh"
dev="${1:-}"
[ -n "$dev" ] || { echo "usage: flash.sh /dev/sdX [image.img]" >&2; exit 2; }
img="${2:-}"
if [ -z "$img" ]; then
  # Newest image that is a whole-card image, not one of the partition images.
  for f in "$HH_AOSP_ROOT"/out/target/product/rpi5/*.img; do
    case "$(basename "$f")" in boot.img|system.img|vendor.img|userdata.img|ramdisk.img|vbmeta.img) continue ;; esac
    [ -f "$f" ] || continue
    if [ -z "$img" ] || [ "$f" -nt "$img" ]; then img="$f"; fi
  done
fi
[ -n "$img" ] && [ -f "$img" ] || { echo "no flashable image found; pass one explicitly" >&2; exit 1; }
[ -b "$dev" ] || { echo "$dev is not a block device" >&2; exit 1; }

echo "image:  $img ($(du -h "$img" | cut -f1))"
echo "target: $dev"
lsblk -o NAME,SIZE,MODEL,TRAN,MOUNTPOINTS "$dev"
if lsblk -no MOUNTPOINTS "$dev" | grep -q .; then echo "unmount $dev's partitions first" >&2; exit 1; fi
read -r -p "Type the device name ($dev) to erase it: " confirm
[ "$confirm" = "$dev" ] || { echo "aborted"; exit 1; }

sudo dd if="$img" of="$dev" bs=4M status=progress conv=fsync
sync
echo "done. Put the card in the Pi 5 and power it."
