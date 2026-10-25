# ADR 0002: Scope of the first install (2003 Eclipse)

Status: Accepted. 2026-10-25.

## Decisions

1. **Amplifier.** The factory Infinity amp is dead. It will be bypassed and
   replaced with an aftermarket amplifier fed line-level from the head unit.
   Consequence: no amp-retention harness; the speaker wiring is re-terminated
   at the new amp. The audio HAL is a single zone with a line-out DAC.
2. **No FM tuner.** Media is Bluetooth and streaming only. Consequence: no
   tuner module, no antenna adapter, no radio app.
3. **Reverse camera: deferred.** Not in the first install. Consequence: M3
   still proves suspend/resume, but the camera path and the EVS work wait.
4. **Bridge board budget and size: deferred.** The bench rig can use a dev
   board. A custom PCB is a M6 question.

## Why

Each of these removes a hardware dependency from the critical path to the
first drive. The amp and tuner are the two most wiring-intensive parts of a
classic head-unit install, and neither affects the software architecture.

## Revisit when

- The first drive is done and the kit is being generalised (M6).
- Anyone wants a camera, at which point boot and resume numbers from M3
  decide whether Android can own it.
