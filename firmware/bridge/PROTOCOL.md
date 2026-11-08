# Bridge protocol

The bridge is the small microcontroller between the car and Android. It
reads the car's discrete 12 V lines, polls the diagnostic port, watches the
battery, and controls the head unit's power. It talks to Android over USB
CDC serial. This document is the contract; the firmware in `src/` and the
Vehicle HAL in `platform/vhal` are its two implementations.

## Design rules

- **Text, one frame per line.** You can debug the whole system with a
  terminal program. Bandwidth is not a concern: the car produces a few
  hundred bytes per second.
- **Every frame is checksummed.** USB is reliable, but the bridge may be
  rebooting, half-plugged, or replaced by a test fixture.
- **The bridge is dumb, Android is smart.** The bridge reports what it
  measures in SI units and raw states. Thresholds, hysteresis, unit
  conversion and policy live in the VHAL, where they can be changed by an
  OTA instead of a firmware flash.
- **The bridge owns power.** Android cannot cut its own supply, and the Pi
  cannot sleep (ADR 0003). The bridge holds 5 V after key-off, tells Android
  to shut down, waits for it to confirm or time out, then cuts power.

## Framing

```
$TYPE,field1,field2,...*HH\n
```

- `$` starts a frame; `\n` ends it. `\r` before `\n` is ignored.
- `TYPE` is 2 to 4 upper-case ASCII letters.
- Fields are comma-separated ASCII; empty field means "unknown".
- `*HH` is the two-hex-digit XOR of every byte between `$` and `*`,
  exclusive (the NMEA 0183 checksum). Lower or upper case accepted.
- Maximum frame length 96 bytes. Longer frames are dropped.
- A receiver ignores frames it does not understand. Unknown trailing fields
  are ignored, so fields may be appended in later versions.

Example: `$DI,1,1,0,1,0*10`

## Bridge to Android

| Type | Fields | Rate | Meaning |
|------|--------|------|---------|
| `HB` | `proto`, `fw`, `uptime_s`, `mode` | 1 Hz | Heartbeat. `proto` is this document's version (`1`). `fw` is the firmware version string. `mode` is `live` or `sim`. Android treats 3 s without `HB` as bridge lost. |
| `DI` | `acc`, `ign`, `rev`, `illum`, `pbrk` | on change, and 2 Hz | Discrete inputs, `0`/`1`, debounced 50 ms. acc = accessory, ign = ignition run, rev = reverse lamp, illum = dash illumination, pbrk = parking brake. |
| `BAT` | `volts` | 1 Hz | Battery voltage at the bridge, one decimal. |
| `SPD` | `kph`, `src` | 5 Hz or on OBD poll | Vehicle speed in km/h, one decimal. `src` is `obd` or `vss` (pulse input). |
| `RPM` | `rpm` | on OBD poll | Engine speed, integer. |
| `ECT` | `celsius` | on OBD poll | Coolant temperature, integer. |
| `FUEL` | `percent` | on OBD poll | Fuel level 0..100, integer, or empty when the ECU does not report PID 0x2F. |
| `OBD` | `status`, `protocol` | on change | `status` is `init`, `ok`, `nocomm`, `off`. `protocol` is e.g. `ISO9141-2`, `KWP2000`, `CAN11-500`. |
| `PWR` | `state`, `countdown_s` | on change, and 1 Hz while not `on` | Power state machine: `on`, `keyoff` (ACC dropped, countdown running), `shutdown` (Android asked to shut down), `cut` (about to remove 5 V). |
| `LOG` | `level`, `text` | as needed | `level` is `I`, `W`, `E`. Human text, no commas. |

## Android to bridge

| Type | Fields | Meaning |
|------|--------|---------|
| `PING` | | Bridge answers with an immediate `HB`. |
| `CFG` | `key`, `value` | Set a runtime parameter. Keys: `keyoff_delay_s` (default 30, how long after ACC drops before `shutdown` is requested), `shutdown_timeout_s` (default 20, how long to wait for `BYE` before cutting), `lowbat_cut_v` (default 11.8, cut power below this while in `keyoff`). Stored in flash. |
| `GET` | `key` | Bridge answers `CFG,key,value`. |
| `BYE` | | Android has finished shutting down and the bridge may cut power now. Only honoured in `shutdown`. |
| `HOLD` | `seconds` | Postpone the cut, e.g. while an OTA finishes. Capped at 600. |
| `SIM` | `on` or `off` | Bench only. In `sim` the bridge invents plausible speed, RPM and temperature and ignores the diagnostic port. The `HB` `mode` field says which is active. |
| `RST` | | Reboot the bridge. |

## Power sequence

```
ACC high ──► PWR,on            Android boots or keeps running.
ACC low  ──► PWR,keyoff,30     countdown; ACC high again cancels it.
countdown 0 ─► PWR,shutdown,20 Android must shut down: VHAL sees this and
                                requests SHUTDOWN_PREPARE through CarPowerManager.
BYE or timeout ─► PWR,cut,3    three seconds later the bridge opens the 5 V rail.
```

Bridge boots on ACC and reports `PWR,on`. While in `keyoff`, if `BAT`
falls below `lowbat_cut_v`, the bridge skips straight to `shutdown` with a
5 s timeout, so a flat battery costs the car a clean shutdown but not the
starter.

## What the Vehicle HAL maps

| Frame | VehicleProperty |
|-------|-----------------|
| `SPD` | `PERF_VEHICLE_SPEED` (m/s = kph / 3.6) |
| `RPM` | `ENGINE_RPM` |
| `ECT` | `ENGINE_COOLANT_TEMP` |
| `FUEL` | `FUEL_LEVEL` (mL = percent/100 x `INFO_FUEL_CAPACITY`) |
| `DI.rev` | `GEAR_SELECTION` (`REVERSE` when 1, otherwise `DRIVE`) |
| `DI.illum` | `NIGHT_MODE` |
| `DI.pbrk` | `PARKING_BRAKE_ON` |
| `DI.ign` | `IGNITION_STATE` (`ON` / `ACC` / `OFF`) |
| `BAT` | `EV_BATTERY_LEVEL` is wrong for a 12 V system; exposed as a vendor property `VENDOR_BRIDGE_BATTERY_VOLTS` |
| `PWR` | `AP_POWER_STATE_REQ` (`SHUTDOWN_PREPARE` on `shutdown`) and `AP_POWER_STATE_REPORT` drives `BYE` |

## Versioning

`proto` in `HB` is bumped when a field's meaning changes or a frame is
removed. Appending fields or adding frame types does not bump it.
