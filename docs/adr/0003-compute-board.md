# ADR 0003: Compute board

Status: Accepted. 2026-11-07. Development and first-install board is the
Raspberry Pi 5. An RK3588 board stays the production candidate for a kit
sold to others; decide that at M6 with bench data.

## Options considered

| Board | For | Against |
|-------|-----|---------|
| Raspberry Pi 5 | Most active community AAOS port, maintained through Android 17. GPIO, I2C, I2S, SPI for the bench. Cheap. Everyone has one. | Not automotive-grade. No real suspend-to-RAM, so key-off power strategy is harder (M3). DSI panels are Pi-specific; HDMI panels need a second cable and USB touch. |
| RK3588 SBC (Radxa Rock 5B, Orange Pi 5 class) | Rockchip ships an Android BSP. Fast. HDMI, DSI, PCIe, plenty of USB. Industrial-temperature variants exist. | Smaller AAOS community. BSP quality varies by vendor. |
| Snapdragon automotive dev kit (SA8155/SA8295) | The real thing. Fast boot, automotive power management, safety islands. | Cost. NDA-gated BSPs. Overkill for a retrofit kit. |

## Decision

Raspberry Pi 5, 8 GB, built from the **raspberry-vanilla** AOSP device tree.

## What makes the Pi 5 viable (verified 2026-11-07)

- raspberry-vanilla publishes a device tree with a dedicated Android
  Automotive product, `aosp_rpi5_car`, for Android 15 (`android-15.0`
  branch, AOSP tag `android-15.0.0_r32`, lunch
  `aosp_rpi5_car-bp1a-userdebug`), Android 16 (`android-16.0`,
  `android-16.0.0_r4`, `-bp4a-`) and Android 17 (`android-17.0`,
  `android-17.0.0_r1`, `-cp2a-`). The 15 and 16 branches are marked
  unmaintained but still build.
- The car product pulls in `packages/services/Car/car_product/build/car.mk`,
  the reference `android.hardware.automotive.vehicle@V4-default-service`
  VHAL, the CAN HAL service with `canhalctrl`/`canhaldump`/`canhalsend`,
  the EVS v4l2 camera stack, the example audio-control HAL and the Pi
  overlays for CarService, Bluetooth, Wi-Fi and SettingsProvider. That is a
  complete AAOS stack; our VHAL work replaces one service.
- Audio can leave the board over an I2S DAC HAT
  (`persist.vendor.audio.device=dac` plus the DAC's `dtoverlay`), which is the
  line-level output we need for the replacement amplifier (ADR 0002).
- Displays: any HDMI panel that reports EDID, with
  `vendor.hwc.drm.force_mode` and `TARGET_SCREEN_DENSITY` to match, or the
  official 7" DSI panels. I2C, I2S and SPI are one `dtparam` each in
  `boot/config.txt`.
- The kit is a product, so we build from source. KonstaKANG's prebuilt Pi
  images forbid commercial use; raspberry-vanilla is the source they are
  built from.

## Android version

M2 builds the `android-15.0` branch so the Pi matches the emulator pin in
ADR 0001 and the CarSystemUI overlay in `platform/overlays`, which was
written against the Android 15 image. We re-pin everything in one step
when Google publishes an Android 16 or 17 automotive emulator image or when
a Pi fix forces it. Expect that re-pin to be the first thing M3 does if
Android 15's Pi branch bit-rots.

## Consequences

- The AOSP build needs a real build host: Ubuntu 22.04, 16+ cores, 64 GB
  RAM, 400 GB free. `tools/rpi5/` scripts the host setup, sync, build and
  flash so it is repeatable there.
- Our apps keep installing over adb (same scripts as the emulator) until M5
  bakes them into the image.
- Key-off power is the Pi's weak point and is M3's first job: the bridge
  board owns the 5 V rail and the Pi gets a clean shutdown signal, because
  the Pi cannot suspend-to-RAM.
- The RK3588 question is not closed. The UI, VHAL and bridge are
  board-agnostic by design, so switching boards later costs a device tree,
  not a rewrite.
