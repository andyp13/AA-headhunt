#!/usr/bin/env bash
# Build the overlays, push them to /product/overlay on the emulator, enable them.
# Needs a userdebug image (the AAOS emulator is one) so adb root and remount work.
set -euo pipefail
# shellcheck source=tools/emulator/env.sh
source "$(dirname "$0")/env.sh"
# shellcheck source=tools/emulator/lib.sh
source "$(dirname "$0")/lib.sh"
root="$(cd "$(dirname "$0")/../.." && pwd)"

require_device
pick_java
"$root/gradlew" -p "$root" :platform:overlays:car-systemui:assembleDebug

adb root >/dev/null
adb wait-for-device
if ! adb remount 2>&1 | grep -qiE "remount succeeded|already"; then
  echo "remount needed a reboot; rebooting and retrying" >&2
  adb reboot && adb wait-for-device && adb root >/dev/null && adb wait-for-device && adb remount
fi

apk="$root/platform/overlays/car-systemui/build/outputs/apk/debug/car-systemui-debug.apk"
adb push "$apk" /product/overlay/HeadhuntSystemUI.apk >/dev/null
adb shell "chmod 644 /product/overlay/HeadhuntSystemUI.apk"

# A freshly pushed overlay is only discovered on boot.
adb reboot
adb wait-for-device
echo "== waiting for the emulator to boot"
for _ in $(seq 1 90); do
  [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ] && break
  sleep 2
done
adb shell cmd overlay enable --user 0 dev.headhunt.overlay.systemui
adb shell cmd overlay list | grep -i headhunt
