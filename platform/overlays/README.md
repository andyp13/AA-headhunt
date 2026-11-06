# platform/overlays

Runtime Resource Overlays that restyle stock AAOS system apps. See ADR 0004.

| Module | Target | What it changes |
|--------|--------|-----------------|
| `car-systemui/` | `com.android.systemui` | Colours and the set of visible system bars. First cut: hide the HVAC bar and match the launcher palette. |

## Where the resource names come from

Overlays may only override resources the target lists in its `<overlayable>`
block, and must use the exact names the pinned image ships. We never guess:

1. From a running emulator: `tools/emulator/dump-sysui-res.sh` pulls
   `CarSystemUI.apk` and dumps its resource table and overlayable policies.
2. Without an emulator: `tools/emulator/extract-sysui-from-image.sh` reads
   `system.img` straight out of the SDK's system-image package with `debugfs`
   and dumps the same tables. This is how the first overlay was written, in a
   container that cannot run the emulator.

The dump that this overlay was written against is committed next to it as
`car-systemui/RESOURCES.md` so a re-pin can be diffed instead of rediscovered.

## Installing on the emulator

`tools/emulator/install-overlays.sh` builds the overlay, pushes it to
`/product/overlay/`, reboots, and enables it with `cmd overlay`. Pushing to
`/product/overlay` satisfies the `product` overlayable policy; platform signing
satisfies `signature`. A plain `adb install` is not enough for either.
