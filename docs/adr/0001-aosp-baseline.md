# ADR 0001: AOSP baseline and emulator pin

Status: Accepted. 2026-10-25.

## Decision

- Platform: AOSP Android Automotive, Android 15. We pin the emulator image
  `system-images;android-35-ext15;android-automotive;x86_64`, `compileSdk 35`,
  and command-line tools build 16111833. These live in `tools/emulator/env.sh`
  and `gradle/libs.versions.toml` and are bumped together.
- No Google Automotive Services. Bare AOSP. Maps, voice and a store are
  app-layer problems we solve later with third-party or open components.
- Apps are built with Gradle and Compose and signed with the platform key.
  The AOSP tree build (Soong) is introduced at M5 when we produce a full image.
  Until then the platform is Google's reference emulator image.

## Why

Android 15 is the newest release with a published automotive emulator image at
the time of writing. Android 16 automotive images were not available from the
SDK repository when checked, so pinning 16 would mean building the emulator
ourselves, which is M5 work.

Gradle first keeps the inner loop at seconds, not the tens of minutes an AOSP
incremental build costs, and the launcher and apps don't need Soong until they
are baked into an image.

## Consequences

- `useLibrary("android.car")` in car-kit resolves to
  `platforms/android-35/optional/android.car.jar`, which the SDK ships.
- Signature-level car permissions require platform signing. For the emulator
  that is the public AOSP test key (see `tools/keys/`). The product image gets
  its own keys and this ADR will be superseded then.
- When Android 16 automotive images appear we re-pin in one commit and re-run
  M0 verification.
