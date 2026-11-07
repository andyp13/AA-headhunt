"""Headhunt bridge frames (firmware/bridge/PROTOCOL.md): build, parse, checksum.

Shared by the bench tools and their tests. Pure Python, no dependencies.
"""
from __future__ import annotations

from dataclasses import dataclass, field

FRAME_MAX = 96


@dataclass
class Frame:
    type: str
    fields: list[str] = field(default_factory=list)

    def encode(self) -> str:
        body = ",".join([self.type, *self.fields])
        line = f"${body}*{checksum(body):02X}\n"
        if len(line) > FRAME_MAX:
            raise ValueError(f"frame too long ({len(line)} > {FRAME_MAX})")
        return line


def checksum(body: str) -> int:
    x = 0
    for b in body.encode("ascii"):
        x ^= b
    return x


def parse(line: str) -> Frame | None:
    """Parse one line. Returns None for anything that is not a valid frame."""
    line = line.strip("\r\n")
    if not line.startswith("$") or "*" not in line or len(line) > FRAME_MAX:
        return None
    body, _, given = line[1:].rpartition("*")
    if len(given) != 2:
        return None
    try:
        if checksum(body) != int(given, 16):
            return None
    except ValueError:
        return None
    parts = body.split(",")
    if not 2 <= len(parts[0]) <= 4 or not parts[0].isalpha() or not parts[0].isupper():
        return None
    return Frame(parts[0], parts[1:])


class Reader:
    """Byte-at-a-time reassembly, mirroring frame_feed() in the firmware."""

    def __init__(self) -> None:
        self._buf: bytearray | None = None

    def feed(self, data: bytes):
        for b in data:
            c = chr(b)
            if c == "$":
                self._buf = bytearray(b"$")
            elif self._buf is None or c == "\r":
                continue
            elif c == "\n":
                frame = parse(self._buf.decode("ascii", "replace"))
                self._buf = None
                if frame:
                    yield frame
            elif len(self._buf) >= FRAME_MAX:
                self._buf = None
            else:
                self._buf.append(b)
