#!/usr/bin/env bash
# Pull CarSystemUI from the running emulator and list its public resources.
# Overlays must target names that exist in the pinned image, so this is the
# source of truth for anything under platform/overlays/.
set -euo pipefail
# shellcheck source=tools/emulator/env.sh
source "$(dirname "$0")/env.sh"
root="$(cd "$(dirname "$0")/../.." && pwd)"
out="$root/build/sysui-res"
mkdir -p "$out"

adb wait-for-device
path=$(adb shell pm path com.android.systemui | sed -n 's/^package://p' | head -1)
[ -n "$path" ] || { echo "com.android.systemui not found on device" >&2; exit 1; }
adb pull "$path" "$out/CarSystemUI.apk" >/dev/null

aapt2="$ANDROID_HOME/build-tools/35.0.0/aapt2"
"$aapt2" dump resources "$out/CarSystemUI.apk" > "$out/resources.txt"
"$aapt2" dump badging "$out/CarSystemUI.apk" | sed -n 1,3p

echo "Resource table: $out/resources.txt"
echo "Candidate overlay targets (bool/colour/dimen/layout names):"
grep -oE 'type (bool|color|dimen|layout|integer)' -A0 "$out/resources.txt" | sort | uniq -c
