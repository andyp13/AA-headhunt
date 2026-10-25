#!/usr/bin/env bash
# Push fake vehicle data into the emulator's VHAL so the UI has something to show.
#
#   inject.sh speed <m/s>      inject.sh rpm <rpm>       inject.sh fuel <0..1>
#   inject.sh gear <P|R|N|D>   inject.sh night <0|1>     inject.sh coolant <C>
#
# Property IDs are the VehicleProperty constants from android.car.VehiclePropertyIds.
# The underlying "cmd car_service inject-vhal-event" syntax has changed between
# releases; this is the android-35 form. If it fails, use the Extended Controls
# > Car Data panel in the emulator window instead.
set -euo pipefail
# shellcheck source=tools/emulator/env.sh
source "$(dirname "$0")/env.sh"

PERF_VEHICLE_SPEED=291504647   # 0x11600207
ENGINE_RPM=291504901           # 0x11600305
ENGINE_COOLANT_TEMP=291504897  # 0x11600301
FUEL_LEVEL=291504903           # 0x11600307
GEAR_SELECTION=289408000       # 0x11400400
NIGHT_MODE=287310855           # 0x11200407
INFO_FUEL_CAPACITY=291504388   # 0x11600104

inject() { adb shell cmd car_service inject-vhal-event "$1" "$2"; }

case "${1:-}" in
  speed)   inject $PERF_VEHICLE_SPEED "$2" ;;
  rpm)     inject $ENGINE_RPM "$2" ;;
  coolant) inject $ENGINE_COOLANT_TEMP "$2" ;;
  fuel)
    cap=$(adb shell cmd car_service get-property-value $INFO_FUEL_CAPACITY 2>/dev/null | grep -oE '[0-9]+(\.[0-9]+)?' | head -1)
    cap="${cap:-60000}"
    inject $FUEL_LEVEL "$(python3 -c "print($cap * $2)")" ;;
  gear)
    case "$2" in P) g=4;; R) g=2;; N) g=1;; D) g=8;; *) echo "gear must be P R N D" >&2; exit 2;; esac
    inject $GEAR_SELECTION "$g" ;;
  night)   inject $NIGHT_MODE "$2" ;;
  *) sed -n '2,8p' "$0"; exit 2 ;;
esac
