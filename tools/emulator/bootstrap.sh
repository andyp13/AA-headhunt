#!/usr/bin/env bash
# One-shot setup: Android SDK pieces, the AAOS emulator image, and an AVD.
# Idempotent. Needs: curl, python3, a JDK 17+, and ~6 GB of disk.
set -euo pipefail
# shellcheck source=tools/emulator/env.sh
source "$(dirname "$0")/env.sh"

echo "SDK root: $ANDROID_HOME"
mkdir -p "$ANDROID_HOME/cmdline-tools" "$ANDROID_AVD_HOME"

# The tools ship a native binary, so the package must match the host OS. A
# previous install for the wrong OS fails the --version probe and is replaced.
case "$(uname -s)" in
  Darwin) ct_os="mac" ;;
  Linux)  ct_os="linux" ;;
  *) echo "unsupported host $(uname -s); use WSL2 on Windows" >&2; exit 1 ;;
esac
# --version is answered by the wrapper script itself, so it cannot detect a
# wrong-OS binary. --list_installed has to run the real tool. The marker file
# records which OS the tools were unpacked for.
ct_dir="$ANDROID_HOME/cmdline-tools/latest"
ct_ok=1
[ "$(cat "$ct_dir/.headhunt-os" 2>/dev/null)" = "$ct_os" ] || ct_ok=0
[ $ct_ok = 1 ] && { "$ct_dir/bin/sdkmanager" --list_installed >/dev/null 2>&1 || ct_ok=0; }
if [ $ct_ok = 0 ]; then
  echo "Installing command-line tools build $HH_CMDLINE_TOOLS_BUILD for $ct_os"
  tmp="$(mktemp -d)"
  curl -sSL -o "$tmp/ct.zip" \
    "https://dl.google.com/android/repository/commandlinetools-${ct_os}-${HH_CMDLINE_TOOLS_BUILD}_latest.zip"
  python3 -I -c "import sys,zipfile; zipfile.ZipFile(sys.argv[1]).extractall(sys.argv[2])" "$tmp/ct.zip" "$tmp"
  rm -rf "$ANDROID_HOME/cmdline-tools/latest"
  mv "$tmp/cmdline-tools" "$ct_dir"
  chmod +x "$ct_dir/bin/"*
  printf '%s\n' "$ct_os" > "$ct_dir/.headhunt-os"
  rm -rf "$tmp"
fi

yes | sdkmanager --licenses >/dev/null 2>&1 || true
sdkmanager "platform-tools" "emulator" "platforms;android-35" "build-tools;35.0.0" "$HH_IMAGE"

if ! avdmanager list avd -c 2>/dev/null | grep -qx "$HH_AVD_NAME"; then
  echo "Creating AVD $HH_AVD_NAME"
  echo no | avdmanager create avd -n "$HH_AVD_NAME" -k "$HH_IMAGE" -d "automotive_1024p_landscape"
fi

# Head-unit sized panel and enough RAM for Compose + Car Service.
cfg="$ANDROID_AVD_HOME/$HH_AVD_NAME.avd/config.ini"
sed -i -e '/^hw.ramSize=/d' -e '/^disk.dataPartition.size=/d' -e '/^hw.keyboard=/d' "$cfg"
printf 'hw.ramSize=4096\ndisk.dataPartition.size=6G\nhw.keyboard=yes\n' >> "$cfg"

echo
echo "Done. Next: tools/emulator/run.sh, then tools/keys/make-platform-keystore.sh, then tools/emulator/install-launcher.sh"
