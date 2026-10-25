#!/usr/bin/env bash
# Boot the AAOS emulator. Pass -wipe-data on first boot after an image update.
set -euo pipefail
source "$(dirname "$0")/env.sh"
exec emulator -avd "$HH_AVD_NAME" -gpu auto -no-snapshot-load "$@"
