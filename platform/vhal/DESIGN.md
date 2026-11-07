# Vehicle HAL design

How bridge frames become `VehicleProperty` values that Car Service, and so
the launcher, can read. Status: design. Implementation starts when the Pi
boots AAOS on the bench (M2) and lands in the AOSP tree at M5.

## Where it runs

Android 15's reference VHAL, `android.hardware.automotive.vehicle@V4-default-service`,
is already in the Pi 5 car product (ADR 0003). It is built from
`hardware/interfaces/automotive/vehicle/aidl/impl/` and split cleanly:

- `DefaultVehicleHal` implements the AIDL interface and talks to Car Service.
- `IVehicleHardware` is the seam below it. The reference ships
  `FakeVehicleHardware`, which answers from JSON config files and the
  emulator's "Car Data" panel.

We implement `IVehicleHardware` once, as `BridgeVehicleHardware`, and build
the same default service against it. Nothing above the seam changes, which
is why the launcher works unchanged on the emulator and the car.

## BridgeVehicleHardware

- Owns `/dev/ttyACM0` (udev rule pins the bridge's VID/PID to a stable
  symlink, `/dev/headhunt-bridge`). Reconnects on EIO; the car can unplug it.
- A reader thread runs `frame_feed` logic (same as the firmware's and
  `tools/bridge/hhframe.py`) and converts frames into property updates:

| Frame | Property | Conversion |
|-------|----------|------------|
| `SPD kph` | `PERF_VEHICLE_SPEED` (float, m/s) | kph / 3.6 |
| `RPM` | `ENGINE_RPM` (float) | as is |
| `ECT` | `ENGINE_COOLANT_TEMP` (float, C) | as is |
| `FUEL percent` | `FUEL_LEVEL` (float, mL) | percent / 100 x `INFO_FUEL_CAPACITY` (Eclipse 3G tank: configured per profile, litres x 1000) |
| `DI.rev` | `GEAR_SELECTION` (int) | 1 -> `GEAR_REVERSE`, 0 -> `GEAR_DRIVE` |
| `DI.illum` | `NIGHT_MODE` (bool) | as is, after 2 s hysteresis so tunnels don't flicker the theme |
| `DI.pbrk` | `PARKING_BRAKE_ON` (bool) | as is |
| `DI.ign`, `DI.acc` | `IGNITION_STATE` (int) | ign -> `ON`, acc only -> `ACC`, neither -> `OFF` |
| `BAT volts` | `VENDOR_BRIDGE_BATTERY_VOLTS` (vendor float) | as is |
| `HB` | `VENDOR_BRIDGE_ONLINE` (vendor bool) | true while heartbeats arrive, false 3 s after the last |
| `OBD status` | `VENDOR_OBD_STATUS` (vendor int) | enum |
| `PWR state` | `AP_POWER_STATE_REQ` | below |

- Static properties (`INFO_MAKE`, `INFO_MODEL`, `INFO_MODEL_YEAR`,
  `INFO_FUEL_CAPACITY`, `INFO_FUEL_TYPE`, `INFO_DRIVER_SEAT`) come from the
  car profile JSON (`profiles/eclipse-3g/vehicle.json`), so a second car is
  a second file.
- Unsupported properties are simply not declared, so Car Service reports
  them unavailable instead of lying.

## Power

The bridge owns the rail (PROTOCOL.md). The HAL translates:

- `PWR,shutdown,N` -> `AP_POWER_STATE_REQ = SHUTDOWN_PREPARE` with
  `SHUTDOWN_ONLY` (no suspend on the Pi). Car Service runs its shutdown
  sequence and apps get `onStateChanged`.
- `AP_POWER_STATE_REPORT = SHUTDOWN_START` from Car Service -> HAL sends
  `BYE`. The bridge cuts power 3 s later; Android has already synced.
- An OTA in progress -> HAL sends `HOLD,300` and reports
  `SHUTDOWN_POSTPONE`.
- `PWR,keyoff` is informational; the UI may show "shutting down in N s".
- Display brightness and `DISPLAY_BRIGHTNESS` stay with the Pi's backlight
  control, not the bridge.

## Why not a user-space app reading the serial port

It would work for a demo, but Car Service is what every app, including
Google's and third-party ones, reads vehicle data from. Going through the
VHAL means `CarPropertyManager`, UX restrictions, power management and the
launcher's existing `CarVehicleSource` all just work.

## Development without hardware

`tools/bridge/sim.py` opens a pty that speaks the protocol with a fake
drive cycle and the real power sequence. Point `BridgeVehicleHardware` at
it (`persist.vendor.headhunt.bridge_path`) on the emulator or on a Pi
without the Pico and the whole chain runs.

## Build and integration (M5)

- `platform/vhal/` becomes an AOSP module directory pulled in via the local
  manifest; `aosp_rpi5_car.mk` swaps
  `android.hardware.automotive.vehicle@V4-default-service` for
  `android.hardware.automotive.vehicle@V4-headhunt-service`.
- SEPolicy: the HAL domain needs `tty_device` read/write for the bridge
  symlink. One `te` file in `platform/sepolicy/`.
- The frame codec is tested on the host with the same vectors as
  `tools/bridge/test_hhframe.py`.
