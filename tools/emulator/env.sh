#!/usr/bin/env bash
# Shared settings for the emulator scripts. Source this, don't run it.
#
# Pinned per docs/adr/0001-aosp-baseline.md. Bump both lines together.
export HH_API="android-35-ext15"
# Image architecture follows the host: Apple Silicon and other arm64 machines
# cannot run the x86_64 image. Override with HH_ARCH=x86_64|arm64-v8a.
case "${HH_ARCH:-$(uname -m)}" in
  arm64|aarch64|arm64-v8a) export HH_ARCH="arm64-v8a" ;;
  *)                       export HH_ARCH="x86_64" ;;
esac
export HH_IMAGE="system-images;${HH_API};android-automotive;${HH_ARCH}"
# Newest build published for both macOS and Linux; 16111833 is Linux-only.
export HH_CMDLINE_TOOLS_BUILD="15641748"
export HH_AVD_NAME="headhunt_car"

export ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export ANDROID_AVD_HOME="${ANDROID_AVD_HOME:-$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/.avd}"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"
