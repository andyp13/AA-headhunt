# firmware/bridge

Firmware for the Headhunt bridge: a Raspberry Pi Pico (RP2040) between the
car and the head unit. It reads the discrete 12 V lines, polls the
diagnostic port, watches the battery and controls the 5 V rail. It talks
to Android over USB serial using the frames in `PROTOCOL.md`.

## Build

Needs the Pico SDK (tested with 2.1.1), CMake and `gcc-arm-none-eabi`.

```sh
git clone --depth 1 --branch 2.1.1 https://github.com/raspberrypi/pico-sdk.git ~/pico-sdk
(cd ~/pico-sdk && git submodule update --init --depth 1 lib/tinyusb)
cmake -S firmware/bridge -B firmware/bridge/build -DPICO_SDK_PATH=~/pico-sdk
cmake --build firmware/bridge/build -j
```

Flash: hold BOOTSEL while plugging the Pico in, then copy
`firmware/bridge/build/headhunt_bridge.uf2` to the `RPI-RP2` drive.

## Try it without a car

With the Pico on USB and nothing wired:

```sh
pip install pyserial
tools/bridge/monitor.py /dev/ttyACM0 --send SIM,on     # macOS: /dev/tty.usbmodem*
```

You get `HB` once a second, `DI` twice a second (all zeros until you wire
inputs), `BAT` (0.0 until the divider is connected), and a fake drive cycle
on `SPD`/`RPM`/`ECT`/`FUEL`. Pull `PIN_IN_ACC` (GPIO 2) high with a jumper
to 3V3, then low, and watch the `PWR` sequence run: `keyoff,30` ...
`shutdown,20` ... `cut,3`. Send `BYE` during `shutdown` to cut early.

## Layout

| File | Does |
|------|------|
| `src/main.c` | loop: read frames, poll inputs, report, tick power and OBD |
| `src/frame.c` | framing and checksum, identical to `tools/bridge/hhframe.py` |
| `src/inputs.c` | debounced GPIO inputs, battery ADC |
| `src/power.c` | the power state machine from PROTOCOL.md; drives `PIN_PWR_HOLD` |
| `src/obd.c` | diagnostic port; simulator now, ISO 9141-2 in M2 |
| `src/config.c` | runtime parameters (`CFG`/`GET`); flash persistence is a TODO |
| `src/pins.h` | the pin map, kept in step with `hardware/bench-rig.md` |

## Not done yet

- ISO 9141-2 / KWP2000 on UART0 (needs the transceiver on the bench).
- VSS pulse counting on GPIO 7.
- Config persistence in flash.
- A CI build; it needs the SDK, which is heavy. The firmware is compiled
  locally before each push (see the PR description for the size report).
