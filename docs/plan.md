# Plan

## Scope

A retrofit head-unit kit on Android Automotive OS. The car keeps its analog
cluster, manual HVAC and factory wiring. The kit replaces the radio and adds
a touchscreen, navigation, media, phone and whatever vehicle data the car can
be persuaded to give up.

First install: 2003 Mitsubishi Eclipse (third generation). See
`profiles/eclipse-3g/`.

Decisions already made for the first install (ADR 0002):

- The factory Infinity amplifier is dead and will be replaced.
- No FM tuner.
- Reverse camera and the bridge-board budget are deferred.

## Architecture in one paragraph

Stock AOSP automotive on a small ARM board with a double-DIN touchscreen. A
microcontroller "bridge" on USB serial reads the car's discrete 12V lines
(ignition, accessory, reverse, illumination, parking brake, speed pulse),
polls OBD-II over ISO 9141-2, and manages power. A custom Vehicle HAL maps
bridge messages to standard `VehicleProperty` IDs. Our launcher, system UI
and apps sit on top and only ever talk to car-kit, which wraps
`CarPropertyManager`.

## Milestones

Each ends with something that boots.

| # | Name | Exit criterion |
|---|------|----------------|
| M0 | Emulator | Launcher builds in CI, installs on the AAOS emulator with platform signing, and shows live speed from injected VHAL data. |
| M1 | Launcher and system UI | Home screen with map placeholder, media card and app grid. Car status strip. CarSystemUI restyled with resource overlays (ADR 0004), no fork. |
| M2 | Bench rig | Chosen board runs AAOS on a bench with the display, bench PSU, bridge board and an OBD simulator. First real property from real hardware. |
| M3 | Power and boot | Key-on/key-off handled by bridge firmware. Android suspends and resumes. Boot and resume times measured. Board choice confirmed by data. |
| M4 | Audio and phone | Audio HAL config for a single zone to an aftermarket amp. Bluetooth calls and media. GPS fix. |
| M5 | In the Eclipse | Harness built, dash kit fitted, first drive. |
| M6 | Kit-ization | OTA, a second car profile as a dry run, install docs someone else could follow. |

## M0 breakdown

- [x] Repo layout, Gradle build, version catalog
- [x] design-system: tokens and theme
- [x] car-kit: `VehicleSource` interface, fake, and the real `CarPropertyManager` implementation
- [x] launcher: HOME activity showing vehicle data
- [x] Emulator bootstrap, run, install and inject scripts
- [x] Platform-key signing path
- [x] CI building, testing and linting on every push
- [x] Verified on a machine with the emulator: launcher installs (macOS, 2026-11-07); speed injection and overlay results to be confirmed
- [x] ADR 0003 compute board: Raspberry Pi 5 (decided 2026-11-07)

## M1 breakdown

M1a, launcher (this is in progress):

- [x] design-system components: CarButton, Panel, Readout
- [x] Nav rail: Home, Apps, Media, Phone, Settings
- [x] Home: map placeholder, speed card, media card with transport controls, car status strip
- [x] App grid from PackageManager
- [x] Media card mirrors the active media session (needs platform signing)
- [x] App icon
- [x] ADR 0004 system UI strategy
- [ ] Verified on the emulator: rail navigation, app grid launches an app, media card follows a playing session

M1b, system UI overlays:

- [x] Dump CarSystemUI resources from the android-35 image (`tools/emulator/extract-sysui-from-image.sh`, no emulator needed)
- [x] First overlay: Headhunt palette on the `car_*` tokens, bar backgrounds, HVAC temperature controls collapsed
- [x] Overlay install script for the emulator
- [ ] Verified on the emulator: overlay enables, bars take the palette, temperature controls gone
- [x] Layout override: drop the HVAC button, point Home and Apps at our launcher (see `platform/overlays/car-systemui/RESOURCES.md`)
- [ ] Verified on the emulator: bar shows Home, Phone, Apps, Notifications, Assistant with icons; Home highlights our launcher; Apps opens our grid


## M2 breakdown (bench rig on the Pi 5)

- [x] ADR 0003 accepted: Pi 5, built from raspberry-vanilla `android-15.0`
- [x] `tools/rpi5/`: build-host setup, pinned sync, Headhunt config patch, build, flash
- [x] `platform/rpi5/README.md`: bring-up guide
- [x] `hardware/bench-rig.md`: parts list and wiring for the bench
- [ ] AOSP synced and `aosp_rpi5_car` built on a real build host
- [ ] Pi 5 boots AAOS on the bench display; adb over USB works
- [ ] Launcher and overlay installed on the Pi with the existing emulator scripts
- [ ] I2S DAC line-out confirmed with a tone
- [ ] Bridge dev board enumerates as USB serial on the Pi
- [ ] First real VehicleProperty from the bridge (the VHAL work starts here)

## Risks

- **CarSystemUI drift.** Every AOSP release touches it. Keep our fork's diff small.
- **ISO 9141-2 is slow.** Expect 2 to 5 samples per second for speed and RPM. A speed pulse from the VSS wire is the fast path if we want a live readout.
- **Boot time.** A cold boot in the tens of seconds is fine for a radio, not for a reverse camera. Suspend-to-RAM has to work before a camera is in scope.
- **12V is hostile.** Load dump, cranking sag and ground loops kill boards and inject audio noise. Budget for a proper automotive power stage, not a USB-C PD brick.
