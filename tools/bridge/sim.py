#!/usr/bin/env python3
"""Fake bridge on a pseudo-terminal, for developing the VHAL without hardware.

    tools/bridge/sim.py            # prints the pty path to open as the bridge
    tools/bridge/sim.py --acc 0    # start with the key off

Speaks PROTOCOL.md: HB at 1 Hz, DI at 2 Hz, BAT at 1 Hz, SPD/RPM at 5 Hz,
ECT/FUEL every 2 s, and answers PING/CFG/GET/BYE/HOLD/SIM. The power state
machine is the firmware's, re-implemented, so a VHAL exercised here sees
the same sequence it will see on the Pico. Keys while running: a toggles
ACC, r toggles reverse, i toggles illumination, p toggles parking brake, q quits.
"""
from __future__ import annotations

import argparse
import math
import os
import pty
import select
import sys
import termios
import time
import tty

from hhframe import Frame, Reader


class Sim:
    def __init__(self, acc: bool) -> None:
        self.di = {"acc": acc, "ign": acc, "rev": False, "illum": False, "pbrk": True}
        self.cfg = {"keyoff_delay_s": 30, "shutdown_timeout_s": 20, "lowbat_cut_v": 11.8}
        self.state, self.deadline, self.bye = "on", 0.0, False
        self.t0 = time.time()
        self.last = {k: 0.0 for k in ("hb", "di", "bat", "fast", "slow", "pwr")}
        self.out: list[Frame] = [Frame("OBD", ["ok", "SIM"]), Frame("PWR", ["on", "0"])]

    def send(self, t: str, *fields) -> None:
        self.out.append(Frame(t, [str(f) for f in fields]))

    def handle(self, f: Frame) -> None:
        if f.type == "PING":
            self.send("HB", 1, "sim", int(time.time() - self.t0), "sim")
        elif f.type == "CFG" and len(f.fields) >= 2 and f.fields[0] in self.cfg:
            self.cfg[f.fields[0]] = type(self.cfg[f.fields[0]])(f.fields[1]); self.send("CFG", *f.fields[:2])
        elif f.type == "GET" and f.fields and f.fields[0] in self.cfg:
            self.send("CFG", f.fields[0], self.cfg[f.fields[0]])
        elif f.type == "BYE":
            self.bye = True
        elif f.type == "HOLD" and f.fields:
            self.deadline += min(int(f.fields[0]), 600)
        elif f.type in ("SIM", "RST"):
            self.send("LOG", "I", f"{f.type} acknowledged")
        else:
            self.send("LOG", "W", f"unknown frame {f.type}")

    def enter(self, state: str, duration: float, now: float) -> None:
        self.state, self.deadline = state, now + duration
        self.report(now)

    def report(self, now: float) -> None:
        left = max(0, math.ceil(self.deadline - now)) if self.state != "on" else 0
        self.send("PWR", self.state, left); self.last["pwr"] = now

    def tick(self, now: float) -> None:
        acc = self.di["acc"]
        if self.state == "on" and not acc:
            self.enter("keyoff", self.cfg["keyoff_delay_s"], now)
        elif self.state == "keyoff":
            if acc:
                self.enter("on", 0, now)
            elif now >= self.deadline:
                self.bye = False; self.enter("shutdown", self.cfg["shutdown_timeout_s"], now)
        elif self.state == "shutdown" and (self.bye or now >= self.deadline):
            self.enter("cut", 3, now)
        elif self.state == "cut" and now >= self.deadline:
            self.send("LOG", "I", "rail off (simulated); set ACC to restart")
            self.enter("off", 1e9, now)
        elif self.state == "off" and acc:
            self.enter("on", 0, now)
        if self.state not in ("on", "off") and now - self.last["pwr"] >= 1:
            self.report(now)

        t = now - self.t0
        if now - self.last["hb"] >= 1:
            self.send("HB", 1, "sim", int(t), "sim"); self.last["hb"] = now
        if now - self.last["di"] >= 0.5:
            self.send("DI", *(int(v) for v in self.di.values())); self.last["di"] = now
        if now - self.last["bat"] >= 1:
            self.send("BAT", f"{14.1 if self.di['ign'] else 12.4:.1f}"); self.last["bat"] = now
        if self.di["ign"] and now - self.last["fast"] >= 0.2:
            kph = max(0.0, 50 + 50 * math.sin(t / 10))
            self.send("SPD", f"{kph:.1f}", "obd"); self.send("RPM", int(850 + kph * 32)); self.last["fast"] = now
        if self.di["ign"] and now - self.last["slow"] >= 2:
            self.send("ECT", int(min(90, 20 + t / 3))); self.send("FUEL", int(max(5, 62 - t / 120))); self.last["slow"] = now


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--acc", type=int, default=1)
    args = ap.parse_args()
    master, slave = pty.openpty()
    print(f"bridge sim on {os.ttyname(slave)}  (a=ACC r=REV i=ILLUM p=PBRK q=quit)", flush=True)

    sim, reader = Sim(bool(args.acc)), Reader()
    stdin = sys.stdin.fileno()
    old = termios.tcgetattr(stdin) if sys.stdin.isatty() else None
    try:
        if old:
            tty.setcbreak(stdin)
        while True:
            now = time.time()
            sim.tick(now)
            for f in sim.out:
                os.write(master, f.encode().encode())
            sim.out.clear()
            r, _, _ = select.select([master] + ([stdin] if old else []), [], [], 0.05)
            if master in r:
                for f in reader.feed(os.read(master, 256)):
                    sim.handle(f)
            if old and stdin in r:
                k = os.read(stdin, 1).decode(errors="ignore")
                if k == "q":
                    break
                for key, name in (("a", "acc"), ("r", "rev"), ("i", "illum"), ("p", "pbrk")):
                    if k == key:
                        sim.di[name] = not sim.di[name]
                        if name == "acc":
                            sim.di["ign"] = sim.di["acc"]
    finally:
        if old:
            termios.tcsetattr(stdin, termios.TCSADRAIN, old)


if __name__ == "__main__":
    main()
