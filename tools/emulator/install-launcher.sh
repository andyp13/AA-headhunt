#!/usr/bin/env bash
# Build the launcher, install it on the running emulator, and make it the home app.
set -euo pipefail
# shellcheck source=tools/emulator/env.sh
source "$(dirname "$0")/env.sh"
# shellcheck source=tools/emulator/lib.sh
source "$(dirname "$0")/lib.sh"
root="$(cd "$(dirname "$0")/../.." && pwd)"

if [ ! -f "$root/tools/keys/platform.p12" ]; then
  echo "warning: tools/keys/platform.p12 missing; the APK will be debug-signed and" >&2
  echo "         Car Service will deny the privileged vehicle properties." >&2
  echo "         Run tools/keys/make-platform-keystore.sh first." >&2
fi

require_device
echo "== building and installing the launcher (first Gradle run can take a few minutes)"
"$root/gradlew" -p "$root" :apps:launcher:installDebug
echo "== making it the home app"

# Point the HOME role at us. The stock CarLauncher stays installed as a fallback.
adb shell cmd package set-home-activity dev.headhunt.launcher/.HomeActivity
adb shell am start -a android.intent.action.MAIN -c android.intent.category.HOME

echo
echo "Installed. Inject some data with tools/emulator/inject.sh speed 25"
