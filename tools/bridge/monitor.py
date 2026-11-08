#!/usr/bin/env python3
"""Watch a bridge over serial and print decoded frames.

    tools/bridge/monitor.py /dev/ttyACM0          # macOS: /dev/tty.usbmodem*
    tools/bridge/monitor.py /dev/ttyACM0 --send 'SIM,on'

Needs pyserial (pip install pyserial). Frames that fail the checksum are
counted and shown, which is the first thing to look at on a noisy bench.
"""
from __future__ import annotations

import argparse
import sys
import time

from hhframe import Frame, Reader, parse

try:
    import serial  # type: ignore
except ImportError:  # pragma: no cover
    sys.exit("pyserial missing: pip install pyserial")


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("port")
    ap.add_argument("--send", action="append", default=[], help="TYPE,field,... to send once connected")
    args = ap.parse_args()

    with serial.Serial(args.port, 115200, timeout=0.1) as s:
        for spec in args.send:
            t, _, rest = spec.partition(",")
            s.write(Frame(t, rest.split(",") if rest else []).encode().encode())
        reader, bad, t0 = Reader(), 0, time.time()
        while True:
            data = s.read(256)
            if not data:
                continue
            for line in data.split(b"\n"):
                if line.startswith(b"$") and parse(line.decode("ascii", "replace") + "\n") is None and b"*" in line:
                    bad += 1
            for f in reader.feed(data):
                print(f"{time.time() - t0:8.2f}  {f.type:<4} {' '.join(f.fields)}" + (f"   [bad={bad}]" if bad else ""))


if __name__ == "__main__":
    main()
