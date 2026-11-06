# Profile: Mitsubishi Eclipse, third generation (2000-2005)

First install target: 2003 model year.

Everything here is **to be verified on the car** unless marked confirmed.
Pre-CAN cars vary by trim and by what a previous owner did to the wiring.

## What the car gives us

| Signal | Source | Path to Android | Status |
|--------|--------|-----------------|--------|
| Vehicle speed | OBD-II PID 0x0D | bridge -> VHAL `PERF_VEHICLE_SPEED` | to verify |
| Vehicle speed (fast) | VSS pulse wire at the cluster | bridge pulse counter -> VHAL | to verify, optional |
| Engine RPM | OBD-II PID 0x0C | bridge -> VHAL `ENGINE_RPM` | to verify |
| Coolant temp | OBD-II PID 0x05 | bridge -> VHAL `ENGINE_COOLANT_TEMP` | to verify |
| Fuel level | OBD-II PID 0x2F, or sender wire | bridge -> VHAL `FUEL_LEVEL` | to verify |
| Reverse | reverse-lamp +12V | bridge -> VHAL `GEAR_SELECTION` (REVERSE / DRIVE) | to verify |
| Illumination / dimmer | dash illumination +12V | bridge -> VHAL `NIGHT_MODE` | to verify |
| Ignition / accessory | ACC and IGN at the radio harness | bridge power controller -> VHAL power states | to verify |
| Parking brake | switch to ground | bridge -> VHAL `PARKING_BRAKE_ON` | to verify |
| Steering wheel controls | likely absent on this generation | n/a | to verify |

## Diagnostic bus

Expected: ISO 9141-2 (Mitsubishi MUT-II era), not CAN. Confirm by plugging in
any cheap scanner and reading the protocol it negotiates. If it reports
ISO 9141-2 or KWP2000, plan on roughly 2 to 5 PID reads per second. If it
reports CAN, this profile gets simpler.

## Dash

- Double-DIN opening with a trim kit (Metra 99-7011 family or equivalent).
  Confirm the exact kit for the 2003 trim.
- Factory Infinity amp: present on this car but dead. Being replaced (ADR 0002).
  The factory harness amp-turn-on and speaker-level lines are therefore
  unused; speakers are re-terminated at the new amp.
- No FM antenna adapter needed (ADR 0002).

## Harness pinout

To be filled in from the car with a meter. Record the radio connector pin
numbers, wire colours and measured voltages with key off, ACC, IGN and
engine running.

| Pin | Colour | Function | Key off | ACC | IGN | Notes |
|-----|--------|----------|---------|-----|-----|-------|
| | | | | | | |

## Open items

- Confirm diagnostic protocol.
- Confirm whether PID 0x2F (fuel level) is supported by this ECU.
- Locate the VSS wire if a fast speed readout is wanted.
- Measure the illumination line: is it PWM-dimmed or switched?
