# Raspberry Pi 5 bring-up

How the bench rig (M2) gets Android Automotive onto a Pi 5. Decision and
rationale: ADR 0003. Parts and wiring: `hardware/bench-rig.md`.

We build from the **raspberry-vanilla** device tree (Apache-2.0), not from
prebuilt images, because the kit is a product and the popular prebuilts
forbid commercial use.

## Pins

| What | Value | Where |
|------|-------|-------|
| raspberry-vanilla branch | `android-15.0` | `tools/rpi5/env.sh` |
| AOSP tag | `android-15.0.0_r32` | same |
| lunch target | `aosp_rpi5_car-bp1a-userdebug` | same |

Android 15 matches the emulator pin (ADR 0001) and the CarSystemUI overlay.
raspberry-vanilla has moved on to Android 17 (`android-17.0`,
`android-17.0.0_r1`, `aosp_rpi5_car-cp2a-userdebug`); the 15 branch is
marked unmaintained but builds. Re-pin all three repos together.

## Build host

Not this laptop, and not the Pi. AOSP wants Ubuntu 22.04, 16+ cores, 64 GB
RAM and about 400 GB of disk. A cloud VM for a day is the cheap route.

```sh
tools/rpi5/setup-build-host.sh        # packages, repo tool, git identity (once)
tools/rpi5/sync.sh --shallow          # AOSP at the pinned tag + Pi manifests (~1-2 h)
tools/rpi5/apply-headhunt-config.sh   # display, density, audio, buses (see below)
tools/rpi5/build.sh                   # hours the first time
tools/rpi5/flash.sh /dev/sdX          # microSD
```

The scripts were written from the AOSP requirements page and the
raspberry-vanilla README and wiki. Their first real run is the first M2
checkbox in `docs/plan.md`; expect to fix small things then.

## What `apply-headhunt-config.sh` changes in `device/brcm/rpi5`

| File | Change | Driven by |
|------|--------|-----------|
| `boot/config.txt` | `dtparam=i2c_arm=on`, `dtparam=i2s=on`, `dtparam=spi=on` | the DAC and any HAT |
| `boot/config.txt` | DSI panel `dtoverlay` on or off | `HH_DISPLAY` |
| `boot/config.txt` | `dtoverlay=hifiberry-dacplus` (or your DAC) | `HH_AUDIO=dac`, `HH_DAC_OVERLAY` |
| `vendor.prop` | `vendor.hwc.drm.force_mode=` panel mode | `HH_DISPLAY`, `HH_HDMI_MODE` |
| `vendor.prop` | `persist.vendor.audio.device=dac` | `HH_AUDIO` |
| `BoardConfig.mk` | `TARGET_SCREEN_DENSITY :=` | `HH_DENSITY` |

Stock values for reference: HDMI0 audio, `1920x1080`, density 240, all
buses off.

### Display choice

- **HDMI panel (default).** Any panel that reports EDID. A 7" 1024x600
  capacitive panel with USB touch is the double-DIN shape; set
  `HH_HDMI_MODE=1024x600 HH_DENSITY=160`. Pi 5 HDMI ports are micro-HDMI.
- **Official Touch Display 2** (`HH_DISPLAY=dsi2`). 720x1280 portrait-native,
  so the UI needs rotating; fine for a desk, wrong shape for the dash.
- **Official 7" Touch Display** (`HH_DISPLAY=dsi1`). 800x480. Too coarse for
  the launcher's type scale.

Unplug HDMI when using a DSI panel.

### Audio

`persist.vendor.audio.device=dac` plus the DAC's overlay routes Android's
output to an I2S DAC HAT. That is the line-level feed to the replacement
amplifier (ADR 0002). `hdmi0` is the fallback for a bench monitor with
speakers.

## What the car product already gives us

`aosp_rpi5_car` inherits `car_product/build/car.mk` and adds the reference
`android.hardware.automotive.vehicle@V4-default-service` VHAL, the CAN HAL
(`android.hardware.automotive.can-service` with `canhalctrl`, `canhaldump`,
`canhalsend`), EVS with a v4l2 camera HAL, the example audio-control HAL,
broadcast radio (unused, ADR 0002) and Pi-specific overlays for CarService,
Bluetooth, Wi-Fi and SettingsProvider. Our VHAL (`platform/vhal`, M2+)
replaces the one default service and nothing else.

## After first boot

- **adb.** The build is `userdebug`, so `adb root` and `adb remount` work.
  The USB-C power port doubles as the adb device port when
  `dtoverlay=dwc2,dr_mode=peripheral` is active in `config.txt` (stock). Or
  use `adb connect <pi-ip>:5555` over Wi-Fi.
- **Our apps.** The emulator scripts are plain adb and work unchanged:
  `tools/emulator/install-launcher.sh` and `tools/emulator/install-overlays.sh`.
- **Bridge board.** Plugs into a USB-A port and should appear as
  `/dev/ttyACM0`. Confirm with `adb shell ls /dev/ttyACM*`.

## Known gaps, owned by M3

- **No suspend-to-RAM.** The Pi 5 cannot sleep like a phone. Key-off means
  the bridge holds 5 V while Android shuts down cleanly, then cuts power.
  Cold boot time is therefore the number to measure first.
- **Boot time** on microSD is tens of seconds. An NVMe HAT is the obvious
  lever and costs a PCIe slot we otherwise don't use.
- **Branch rot.** `android-15.0` is unmaintained upstream. If a Pi fix we
  need only lands in 16 or 17, that forces the re-pin.

## Sources

- raspberry-vanilla local manifest and README, branches `android-15.0`,
  `android-16.0`, `android-17.0`:
  https://github.com/raspberry-vanilla/android_local_manifest
- Pi 5 device tree (`aosp_rpi5_car.mk`, `device.mk`, `boot/config.txt`,
  `vendor.prop`, `BoardConfig.mk`):
  https://github.com/raspberry-vanilla/android_device_brcm_rpi5
- Wiki pages Audio, DSI display, HDMI display, Interfaces:
  https://github.com/raspberry-vanilla/android_local_manifest/wiki
- AOSP build host requirements:
  https://source.android.com/docs/setup/start/requirements
- Prebuilt-image licensing note (non-commercial): https://konstakang.com/devices/rpi5/AOSP15/
