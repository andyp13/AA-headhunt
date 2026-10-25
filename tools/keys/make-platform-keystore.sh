#!/usr/bin/env bash
# Fetch the AOSP platform test key and turn it into a PKCS12 keystore Gradle can use.
#
# The AAOS emulator images are AOSP userdebug builds signed with the public test
# keys from build/make/target/product/security. Signing our apps with the same
# platform key is what lets Car Service grant the signature-level permissions
# (CAR_ENGINE_DETAILED and friends). These keys are public and must never be
# used for anything that ships; the product image in M5 gets its own keys.
set -euo pipefail
cd "$(dirname "$0")"

base="https://raw.githubusercontent.com/aosp-mirror/platform_build/main/target/product/security"
curl -sSL -o platform.pk8 "$base/platform.pk8"
curl -sSL -o platform.x509.pem "$base/platform.x509.pem"

openssl pkcs8 -in platform.pk8 -inform DER -outform PEM -nocrypt -out platform.priv.pem
openssl pkcs12 -export -in platform.x509.pem -inkey platform.priv.pem \
  -name platform -password pass:android -out platform.p12
rm -f platform.priv.pem

echo "wrote $(pwd)/platform.p12 (alias platform, password android)"
openssl x509 -in platform.x509.pem -noout -subject -fingerprint -sha256
