# Bench rig (M2)

Everything needed to run the head unit on a desk before it meets the car.
Prices are left out on purpose; check them when ordering. Items marked
"later" are not needed to light the screen up.

## Parts

| # | Part | Notes |
|---|------|-------|
| 1 | Raspberry Pi 5, 8 GB | 4 GB would run AAOS; 8 GB keeps builds of the UI snappy and leaves room for maps later. |
| 2 | Official 27 W USB-C power supply | Bench only. The car gets an automotive buck converter (M3). The Pi 5 wants 5 V at up to 5 A, so phone chargers are not enough. |
| 3 | Active cooler for Pi 5 | The dash gets hot; start with cooling from day one. |
| 4 | microSD, 64 GB, A2 class | First boots. An NVMe HAT + drive is the boot-time lever for M3. |
| 5 | 7" HDMI capacitive touch panel, 1024x600, USB touch | The double-DIN shape. Needs a micro-HDMI to HDMI cable and a USB-A lead for touch. Set `HH_HDMI_MODE=1024x600 HH_DENSITY=160`. |
| 6 | I2S DAC HAT (HiFiBerry DAC+ or equivalent) | Line-level audio out to the amplifier. Uses the I2S pins and I2C; leaves USB free for the bridge. Set `HH_AUDIO=dac`, `HH_DAC_OVERLAY` to the HAT's overlay name. |
| 7 | Bridge dev board: Raspberry Pi Pico (RP2040) or Pico 2 | Appears to the Pi as USB CDC serial. 3.3 V logic, so the car's 12 V inputs need dividers or optocouplers (item 10). Any ESP32-S3 board also works. |
| 8 | USB-C data cable | Pi 5 USB-C port is also the adb device port on this image. |
| 9 | Bench 12 V supply, 5 A | later. Lets the bridge's power-controller firmware be tested with a real "battery" before the car. |
| 10 | Input conditioning for the bridge | later. Per 12 V input: series resistor + divider to 3.3 V with a clamp, or an optocoupler. Reverse, illumination, ACC, IGN, parking brake: five channels. |
| 11 | ISO 9141-2 / K-line transceiver | later. L9637D or an ELM327-class module on UART for the OBD side of the bridge. Confirm the car's protocol first (`profiles/eclipse-3g`). |
| 12 | 12 V to 5 V, 5 A automotive buck converter | later, M3. Must survive load dump and cranking sag; not a USB cigarette-lighter adapter. |

## Wiring (bench)

```
 27 W PSU ──USB-C──► Pi 5 ◄──USB-C data──► laptop (adb)
                       │
                       ├─ micro-HDMI ──► 7" panel ──USB-A──► Pi 5 (touch)
                       │
                       ├─ 40-pin header ──► I2S DAC HAT ──RCA/3.5 mm──► amp or powered speaker
                       │
                       └─ USB-A ──► Pico (bridge) ──GPIO──► bench switches standing in for
                                                            reverse / illumination / ACC / IGN / brake
```

Keep the bridge on USB rather than the Pi's UART pins: the DAC HAT sits on
the header, USB gives us power and a clean reset, and it's exactly how it
will connect in the car.

## First-light checklist

1. Flash the image (`tools/rpi5/flash.sh`), insert the card, connect panel
   and PSU only. Expect the AAOS boot animation, then the stock car launcher.
2. `adb devices` over the USB-C data cable. Then `adb shell getprop ro.build.flavor`
   should say `aosp_rpi5_car-userdebug`.
3. `tools/emulator/install-launcher.sh` and `tools/emulator/install-overlays.sh`.
   Our home screen and palette should appear.
4. Fit the DAC HAT, re-run `apply-headhunt-config.sh` with `HH_AUDIO=dac` if
   it was not already, rebuild or edit `vendor.prop` on the device, play a
   track from Bluetooth, hear it on the line out.
5. Plug the Pico in. `adb shell ls /dev/ttyACM*` lists it. That is the
   starting line for `firmware/bridge` and `platform/vhal`.
