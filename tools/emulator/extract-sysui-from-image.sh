#!/usr/bin/env bash
# Get CarSystemUI's resource table without booting anything.
#
# Reads the SDK's android-automotive system image, which is a GPT disk holding
# a dynamic "super" partition, carves system_ext out of it with the liblp
# metadata, pulls CarSystemUI.apk with debugfs, and dumps its resource table
# and layouts with aapt2. Output lands in build/sysui-res/. Needs: python3,
# debugfs (e2fsprogs), ~2 GB of scratch disk, and the system image installed
# (tools/emulator/bootstrap.sh does that).
set -euo pipefail
# shellcheck source=tools/emulator/env.sh
source "$(dirname "$0")/env.sh"
root="$(cd "$(dirname "$0")/../.." && pwd)"
img="$ANDROID_HOME/system-images/${HH_API}/android-automotive/x86_64/system.img"
out="$root/build/sysui-res"
aapt2="$ANDROID_HOME/build-tools/35.0.0/aapt2"
mkdir -p "$out"
[ -f "$img" ] || { echo "system image not installed: $img" >&2; exit 1; }
command -v debugfs >/dev/null || { echo "debugfs missing (apt-get install e2fsprogs)" >&2; exit 1; }

python3 -I - "$img" "$out/system_ext.ext4" <<'PY'
import struct, sys
img, dest = sys.argv[1], sys.argv[2]
f = open(img, "rb")
# GPT: find the "super" partition.
f.seek(512); h = f.read(92)
assert h[:8] == b"EFI PART", "not a GPT image"
pstart, nparts, psize = struct.unpack_from("<QII", h, 72)
f.seek(pstart * 512); table = f.read(nparts * psize)
super_off = None
for i in range(nparts):
    e = table[i*psize:(i+1)*psize]
    if e[:16] == b"\0"*16: continue
    flba = struct.unpack_from("<Q", e, 32)[0]
    if e[56:128].decode("utf-16le").rstrip("\0") == "super": super_off = flba * 512
assert super_off is not None, "no super partition"
# liblp: geometry at +4096, metadata after two geometry copies.
mo = super_off + 4096 + 2*4096
f.seek(mo); mh = f.read(256)
assert struct.unpack_from("<I", mh, 0)[0] == 0x414C5030, "bad liblp header"
hsz = struct.unpack_from("<I", mh, 8)[0]; tsz = struct.unpack_from("<I", mh, 44)[0]
p_off, p_n, p_sz = struct.unpack_from("<III", mh, 80); e_off, e_n, e_sz = struct.unpack_from("<III", mh, 92)
f.seek(mo + hsz); t = f.read(tsz)
exts = [struct.unpack_from("<QIQI", t, e_off + i*e_sz) for i in range(e_n)]
for i in range(p_n):
    e = t[p_off + i*p_sz:p_off + (i+1)*p_sz]
    name = e[:36].split(b"\0")[0].decode(); _, fei, nex, _ = struct.unpack_from("<IIII", e, 36)
    if name != "system_ext": continue
    with open(dest, "wb") as o:
        for j in range(fei, fei + nex):
            nsec, ttype, tdata, _ = exts[j]
            if ttype != 0: o.write(b"\0" * nsec * 512); continue
            f.seek(super_off + tdata * 512); rem = nsec * 512
            while rem:
                c = f.read(min(rem, 64 << 20)); o.write(c); rem -= len(c)
    print("system_ext ->", dest); break
else:
    raise SystemExit("system_ext not found in super")
PY

debugfs -R "dump /priv-app/CarSystemUI/CarSystemUI.apk $out/CarSystemUI.apk" "$out/system_ext.ext4" >/dev/null 2>&1
rm -f "$out/system_ext.ext4"
"$aapt2" dump badging "$out/CarSystemUI.apk" | sed -n 1p
"$aapt2" dump resources "$out/CarSystemUI.apk" > "$out/resources.txt"
"$aapt2" dump overlayable "$out/CarSystemUI.apk" > "$out/overlayable.txt" || true
for l in car_bottom_system_bar car_top_system_bar adjustable_temperature_view; do
  "$aapt2" dump xmltree --file "res/layout/$l.xml" "$out/CarSystemUI.apk" > "$out/layout_$l.tree" 2>/dev/null || true
done
echo "Resource table: $out/resources.txt ($(grep -c 'resource 0x' "$out/resources.txt") entries)"
echo "Overlayable:    $out/overlayable.txt ($(wc -l < "$out/overlayable.txt") lines; 0 means none declared, everything is overlayable)"
echo "Layouts:        $out/layout_*.tree"
