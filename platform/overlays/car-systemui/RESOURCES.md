# CarSystemUI resources this overlay was written against

Source: `CarSystemUI.apk` (package `com.android.systemui`, versionName 15,
compileSdk 35) from `system_ext/priv-app/CarSystemUI/` in the SDK image
`system-images;android-35-ext15;android-automotive;x86_64`, extracted with
`tools/emulator/extract-sysui-from-image.sh`. Re-run that script after a
re-pin and diff against this file.

`aapt2 dump overlayable` is empty: CarSystemUI declares no `<overlayable>`,
so any resource may be overlaid by a product or platform-signed overlay.

## What we override, and the stock values

| Resource | Stock | Ours | Why |
|----------|-------|------|-----|
| `color/car_background` | `@color/car_neutral_10` (#181c1f) | #0B0D10 | design-system NightBackground |
| `color/car_surface` | `@color/car_neutral_2` (#04080b) | #161A1F | NightSurface |
| `color/car_surface_variant` | `@color/car_neutral_variant_30` | #20252C | |
| `color/car_surface_1..5` | #222429 .. #30333e | #1B2026 .. #2F363F | elevation steps on our base |
| `color/car_on_background`, `car_on_surface` | `@color/car_neutral_90` (#dfe3e7) | #E9EDF1 | NightOnSurface |
| `color/car_on_surface_variant` | `@color/car_neutral_variant_80` | #8A94A0 | NightMuted |
| `color/car_outline`, `car_outline_variant` | neutral_variant 60 / 30 | #5B6670 / #353A3D | |
| `color/car_primary` | `@color/car_primary_80` (#91cef5, light blue) | #FF6A00 | Accent. Drives `car_nav_icon_background_color_selected` and `car_focus_ring`, which already point at `car_primary`. |
| `color/car_on_primary` | `@color/car_primary_20` | #1A0A00 | dark on orange; also `system_bar_icon_selected_color` |
| `color/car_primary_container`, `car_on_primary_container` | primary 30 / 90 | #4A2000 / #FFD8C2 | |
| `color/car_error` | `@color/car_red_80` | #FF6B61 | Danger, lifted for a dark surface |
| `color/car_error_container`, `car_on_error_container` | | #7A1A14 / #FFDAD4 | |
| `color/system_bar_background_opaque` | `@color/car_background` | #0B0D10 | |
| `color/system_bar_background_pill_color` | `@color/car_surface_3` | #252B32 | |
| `color/system_bar_text_color`, `system_bar_clock_text_color` | `@color/car_on_background` | #E9EDF1 | |
| `drawable/nav_bar_background` | `@android:0x0106000c` | #0B0D10 | the bottom bar's background; Google's NavBarTranslucent overlay sets this to #55000000 |
| `drawable/status_bar_background` | `@android:0x0106000c` | #0B0D10 | the top bar's background; same pattern as StatusBarTranslucent |
| `dimen/hvac_container_padding` | 16dp | 0dp | collapse temperature controls |
| `dimen/hvac_temperature_button_size` | 64dp | 0dp | |
| `dimen/hvac_temperature_button_padding` | | 0dp | |
| `dimen/hvac_temperature_text_padding` | 10dp | 0dp | |
| `dimen/hvac_temperature_text_size` | 44sp | 0sp | |

Left alone on purpose: `system_bar_icon_color` (`@android:0x0106000b`),
`car_nav_icon_fill_color` (`@color/car_on_surface`, follows ours), and every
`car_<hue>_<tone>` tonal step. Google's orange theme replaces whole tonal
palettes (163 resources); we override the semantic tokens the UI actually
reads, which is why this overlay is 30 values.

## System bar configuration (stock, for reference)

| Resource | Value |
|----------|-------|
| `bool/config_enableTopSystemBar` | true |
| `bool/config_enableBottomSystemBar` | true |
| `bool/config_enableLeftSystemBar` / `Right` | false |
| `integer/config_topSystemBarType` / `ZOrder` | 0 / 1 |
| `integer/config_bottomSystemBarType` / `ZOrder` | 1 / 10 |
| `integer/config_systemBarPersistency` | 1 |

Google's `CarSystemUIBottomRRO`, `LeftRRO`, `RightRRO` in `/product/overlay`
flip exactly these to move the bar. We keep top + bottom for now.

## How Google's overlays are declared

Every Google RRO in the image uses `android:resourcesMap="@xml/..."` with an
explicit `<overlay><item target="bool/config_enableTopSystemBar" value="@bool/..."/>`
list, `android:isStatic="false"`, no `priority` or `priority="1"`, and most set
`android:category`. Ours matches by name instead, which is simpler and only
touches resources we declare; switch to a `resourcesMap` if we ever need an
overlay resource whose name must differ from the target's.

## Bottom system bar layout (stock), for the next step

`layout/car_bottom_system_bar`, references resolved to names:

```
CarSystemBarView  background=@drawable/nav_bar_background
  RelativeLayout  id=@id/nav_buttons
    TemperatureControlView  id=@id/driver_hvac  app:hvacAreaId=49
      include layout=@layout/adjustable_temperature_view
    LinearLayout  paddingStart/End=@dimen/system_bar_button_group_padding (64dp)
      Space (weight 1)
      CarSystemBarButton  id=@id/home            icon=@drawable/car_ic_home          style=@style/SystemBarButton
      CarSystemBarButton  id=@id/passenger_home  (gone)
      CarSystemBarButton  id=@id/phone_nav       icon=@drawable/car_ic_phone         packages=com.android.car.dialer
      AppGridButton       id=@id/grid_nav        icon=@drawable/car_ic_apps          componentNames=@string/config_appGridComponentName
      CarSystemBarButton  id=@id/hvac            icon=@drawable/car_ic_hvac          app:broadcast=true
      CarSystemBarButton  id=@id/control_center_nav (gone)
      CarSystemBarButton  id=@id/notifications   icon=@drawable/car_ic_notification
      AssistantButton     id=@id/assistant       icon=@drawable/ic_mic_light
      Space (weight 1)
    TemperatureControlView  id=@id/passenger_hvac  app:hvacAreaId=68  layout_alignParentEnd
      include layout=@layout/adjustable_temperature_view
  LinearLayout  id=@id/lock_screen_nav_buttons (gone)
  LinearLayout  id=@id/occlusion_buttons (gone)
    CarSystemBarButton  id=@id/home ...
```

`layout/adjustable_temperature_view` is a horizontal LinearLayout of
`ImageView id/hvac_decrease_button`, `TextView id/hvac_temperature_text`,
`ImageView id/hvac_increase_button`, all sized by the `hvac_*` dimens above.

## Next step: remove the HVAC button and point Home at our launcher

Both need a layout override of `car_bottom_system_bar` (drop the two
`TemperatureControlView`s and `id/hvac`; change `app:componentNames` on
`id/home` to `dev.headhunt.launcher/.HomeActivity` and `id/grid_nav` to our
app grid). The open question is how an overlay layout references target
resources (`@id/home`, `@drawable/car_ic_home`, `@style/SystemBarButton`):
with name matching the overlay must declare each name itself, and a declared
drawable replaces the target's. Options are to declare only ids and copy the
few drawables, or to compile against `CarSystemUI.apk` with `aapt2 -I`, which
would make CI depend on the 5 GB system image. Decide on the emulator with
`tools/emulator/install-overlays.sh`; if SystemUI crash-loops, recover with
`adb shell cmd overlay disable dev.headhunt.overlay.systemui` or by deleting
`/product/overlay/HeadhuntSystemUI.apk`.
