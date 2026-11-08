#!/usr/bin/env bash
# Shared helpers for the emulator scripts. Source after env.sh.

# Fail fast with guidance when no adb device is online, instead of letting
# `adb wait-for-device` hang silently. Waits up to $1 seconds (default 10).
require_device() {
  local timeout="${1:-10}" waited=0 state
  while :; do
    state="$(adb get-state 2>/dev/null || true)"
    [ "$state" = "device" ] && return 0
    if [ "$waited" -ge "$timeout" ]; then
      echo "error: no adb device online after ${timeout}s." >&2
      echo "  adb devices:" >&2
      adb devices 2>&1 | sed 's/^/    /' >&2
      echo "  - Emulator not running?  tools/emulator/run.sh in another terminal, wait for the car launcher." >&2
      echo "  - Device 'offline' or missing while the emulator is up?  Two adb builds may be fighting" >&2
      echo "    (Android Studio's vs $ANDROID_HOME/platform-tools). Run: adb kill-server; adb devices" >&2
      return 1
    fi
    sleep 1; waited=$((waited + 1))
  done
}
