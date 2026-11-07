#!/usr/bin/env bash
# Patch device/brcm/rpi5 for the Headhunt bench rig: display, density, audio,
# and the I2C/I2S/SPI buses the bridge and DAC need. Idempotent; re-run after
# changing HH_* in env.sh or after a repo sync resets the tree.
#
# The edits are the ones the raspberry-vanilla wiki describes (DSI-display,
# HDMI-display, Audio, Interfaces pages), applied by script so they survive
# a re-sync and are visible in one place.
set -euo pipefail
# shellcheck source=tools/rpi5/env.sh
source "$(dirname "$0")/env.sh"
dev="$HH_AOSP_ROOT/device/brcm/rpi5"
[ -d "$dev" ] || { echo "no device tree at $dev; run sync.sh first" >&2; exit 1; }
cfg="$dev/boot/config.txt"; prop="$dev/vendor.prop"; board="$dev/BoardConfig.mk"

enable_line() { # enable_line <file> <exact line without leading #>
  if grep -qxF "$2" "$1"; then return; fi
  if grep -qxF "#$2" "$1"; then sed -i "s|^#$(printf '%s' "$2" | sed 's/[][\\.*^$/|]/\\&/g')\$|$2|" "$1"; else printf '%s\n' "$2" >> "$1"; fi
}
set_prop() { # set_prop <file> <key> <value>
  if grep -qE "^$2=" "$1"; then sed -i "s|^$2=.*|$2=$3|" "$1"; else printf '%s=%s\n' "$2" "$3" >> "$1"; fi
}

echo "== buses"
enable_line "$cfg" "dtparam=i2c_arm=on"
enable_line "$cfg" "dtparam=i2s=on"
enable_line "$cfg" "dtparam=spi=on"

echo "== display: $HH_DISPLAY"
sed -i 's|^dtoverlay=vc4-kms-dsi-7inch$|#dtoverlay=vc4-kms-dsi-7inch|; s|^dtoverlay=vc4-kms-dsi-ili9881-7inch$|#dtoverlay=vc4-kms-dsi-ili9881-7inch|' "$cfg"
case "$HH_DISPLAY" in
  hdmi) set_prop "$prop" vendor.hwc.drm.force_mode "$HH_HDMI_MODE" ;;
  dsi1) enable_line "$cfg" "dtoverlay=vc4-kms-dsi-7inch";          set_prop "$prop" vendor.hwc.drm.force_mode 800x480 ;;
  dsi2) enable_line "$cfg" "dtoverlay=vc4-kms-dsi-ili9881-7inch";  set_prop "$prop" vendor.hwc.drm.force_mode 720x1280 ;;
  *) echo "HH_DISPLAY must be hdmi, dsi1 or dsi2" >&2; exit 2 ;;
esac
sed -i "s|^TARGET_SCREEN_DENSITY := .*|TARGET_SCREEN_DENSITY := $HH_DENSITY|" "$board"

echo "== audio: $HH_AUDIO"
set_prop "$prop" persist.vendor.audio.device "$HH_AUDIO"
if [ "$HH_AUDIO" = dac ]; then enable_line "$cfg" "dtoverlay=$HH_DAC_OVERLAY"; fi

echo "== result"
grep -nE "^dtparam=|^dtoverlay=(vc4-kms-dsi|$HH_DAC_OVERLAY)" "$cfg"
grep -nE "^(persist.vendor.audio.device|vendor.hwc.drm.force_mode)=" "$prop"
grep -n "^TARGET_SCREEN_DENSITY" "$board"
( cd "$dev" && git --no-pager diff --stat ) || true
