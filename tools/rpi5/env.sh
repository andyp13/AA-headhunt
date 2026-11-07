#!/usr/bin/env bash
# Pins and choices for the Raspberry Pi 5 AAOS build. Source this, don't run it.
# Pinned per docs/adr/0003-compute-board.md. Bump the three together.
export HH_RV_BRANCH="android-15.0"
export HH_AOSP_TAG="android-15.0.0_r32"
export HH_LUNCH="aosp_rpi5_car-bp1a-userdebug"

# Where the AOSP tree lives on the build host (needs ~400 GB free).
export HH_AOSP_ROOT="${HH_AOSP_ROOT:-$HOME/aosp-rpi5}"

# Bench hardware choices, applied to the device tree by apply-headhunt-config.sh.
#   HH_DISPLAY   hdmi | dsi1 (official 7" Touch Display, 800x480) | dsi2 (Touch Display 2, 720x1280)
#   HH_HDMI_MODE a mode the panel advertises over EDID; only used for hdmi
#   HH_DENSITY   TARGET_SCREEN_DENSITY; 160 suits a 7" 1024x600 at arm's length
#   HH_AUDIO     dac | hdmi0 | hdmi1 | jack
#   HH_DAC_OVERLAY the DAC HAT's dtoverlay name; only used for dac
export HH_DISPLAY="${HH_DISPLAY:-hdmi}"
export HH_HDMI_MODE="${HH_HDMI_MODE:-1024x600}"
export HH_DENSITY="${HH_DENSITY:-160}"
export HH_AUDIO="${HH_AUDIO:-dac}"
export HH_DAC_OVERLAY="${HH_DAC_OVERLAY:-hifiberry-dacplus}"
