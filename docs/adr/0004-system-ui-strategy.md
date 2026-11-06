# ADR 0004: System UI strategy

Status: Accepted. 2026-11-06.

## Context

AAOS draws the status bar, HVAC bar, notifications, volume and user picker
from CarSystemUI, a fork of SystemUI that Google changes in every release.
Our rule is "overlay before fork". This ADR says what that means for the
system UI specifically.

## Decision

1. **The launcher owns the content area; CarSystemUI keeps the chrome.**
   Our launcher provides the nav rail, map, media and vehicle cards. We do
   not reimplement the status bar or notification shade.
2. **Restyle CarSystemUI with Runtime Resource Overlays, not a fork.**
   Colours, dimensions, icons and the set of visible system bars are all
   resources. An RRO is a plain APK we can build with Gradle, so it fits the
   existing toolchain and survives an AOSP re-pin with at most a resource
   rename.
3. **Drop the HVAC bar.** The Eclipse keeps its manual HVAC knobs, so the
   bar is noise. CarSystemUI's bar layout is resource-driven and an overlay
   can hide it.
4. **Fork only on proof.** If a behaviour we need (not an appearance) cannot
   be reached through resources or a Car Service config, we fork the
   smallest unit possible, record what and why here, and carry the diff as
   patches against the pinned AOSP tag.

## Mechanics

- Overlays live under `platform/overlays/<target>/` as Gradle modules with
  `<overlay android:targetPackage=... />` manifests and no code.
- On the emulator they are pushed to `/product/overlay/` after `adb root &&
  adb remount`, then enabled with `cmd overlay enable`. In the product image
  (M5) they are built in.
- Resource names are taken from the pinned image, not from memory:
  `tools/emulator/extract-sysui-from-image.sh` reads `CarSystemUI.apk` straight
  out of the SDK's system image (no emulator needed) and
  `tools/emulator/dump-sysui-res.sh` does the same from a running one. The
  dump each overlay was written against is committed beside it as `RESOURCES.md`.
- CarSystemUI in the android-35 image declares no `<overlayable>`, so every
  resource is fair game for a product or platform-signed overlay.

## Consequences

- M1 is split. M1a (this PR) is the launcher. M1b builds the first overlay
  against resource names dumped from the android-35 image.
- Platform signing is not required for overlays on a userdebug emulator,
  but it is for the product image, so overlays are signed like the apps.
