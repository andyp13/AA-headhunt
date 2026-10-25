# Headhunt

An Android Automotive OS head-unit kit for cars that predate CAN and
touchscreens. First target: a 2003 Mitsubishi Eclipse.

The platform stays stock AOSP automotive. We own everything the driver sees
(launcher, system UI, core apps), the hardware that goes in the dash, and the
Vehicle HAL that feeds real car data into Android from an OBD-II port and a
handful of wires.

Read [docs/plan.md](docs/plan.md) for the milestones and
[docs/adr](docs/adr) for the decisions behind them.

## Layout

```
apps/launcher/        Home screen. Replaces CarLauncher.
libs/design-system/   Compose tokens, theme and driving-safe components.
libs/car-kit/         The only way apps read vehicle data. Real + fake sources.
tools/emulator/       Bootstrap, run and feed the AAOS emulator.
tools/keys/           Platform signing for the emulator (keys are gitignored).
platform/vhal/        Vehicle HAL that bridges the car to Android.   (M2+)
firmware/bridge/      Microcontroller: discrete inputs, OBD, power.   (M2+)
hardware/             Schematics, harness pinouts, enclosure.         (M2+)
profiles/eclipse-3g/  Everything specific to the 2000-2005 Eclipse.
docs/                 Plan and architecture decision records.
```

## Quick start (M0)

Requirements: JDK 17 or newer, ~6 GB disk, a machine that can run the Android
emulator with hardware acceleration.

```sh
tools/emulator/bootstrap.sh          # SDK, AAOS image, AVD (once)
tools/keys/make-platform-keystore.sh # AOSP platform test key -> tools/keys/platform.p12 (once)
tools/emulator/run.sh                # boot the emulator
tools/emulator/install-launcher.sh   # build, install, set as home
tools/emulator/inject.sh speed 27    # ~60 mph on screen
tools/emulator/inject.sh night 0     # day theme
```

`./gradlew build` compiles everything, runs unit tests and lint. CI runs the
same on every push.

## Rules we build by

- **Overlay before fork.** If a resource overlay or Car Service config can do
  it, do that. Fork a system app only when behaviour has to change.
- **One entry point for car data.** Apps depend on `VehicleSource` from
  car-kit, never on `CarPropertyManager`.
- **Standard properties only.** The Eclipse VHAL publishes the same
  `VehiclePropertyIds` the emulator does, so the UI never knows which car it
  is in.
- **Pin and re-pin.** One AAOS release per pin, bumped deliberately. See ADR 0001.
