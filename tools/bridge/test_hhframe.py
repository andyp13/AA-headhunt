import unittest

from hhframe import Frame, Reader, checksum, parse


class FrameTest(unittest.TestCase):
    def test_checksum_matches_protocol_example(self):
        self.assertEqual(checksum("DI,1,1,0,1,0"), 0x10)

    def test_round_trip(self):
        f = Frame("SPD", ["27.0", "obd"])
        line = f.encode()
        self.assertTrue(line.startswith("$SPD,27.0,obd*"))
        self.assertEqual(parse(line), f)

    def test_empty_field_means_unknown(self):
        self.assertEqual(parse(Frame("FUEL", [""]).encode()), Frame("FUEL", [""]))

    def test_bad_checksum_rejected(self):
        self.assertIsNone(parse("$DI,1,1,0,1,0*00\n"))

    def test_lowercase_checksum_accepted(self):
        body = "RPM,850"  # checksum 0x5e
        hx = f"{checksum(body):02x}"
        self.assertTrue(any(c.isalpha() for c in hx), "pick a body whose checksum has a hex letter")
        self.assertEqual(parse(f"${body}*{hx}"), Frame("RPM", ["850"]))

    def test_type_must_be_two_to_four_upper_letters(self):
        self.assertIsNone(parse("$x,1*" + f"{checksum('x,1'):02X}"))
        self.assertIsNone(parse("$TOOLONG*" + f"{checksum('TOOLONG'):02X}"))

    def test_reader_handles_fragments_and_junk(self):
        r = Reader()
        got = list(r.feed(b"garbage$HB,1,0."))
        self.assertEqual(got, [])
        got += list(r.feed(b"1.0,5,sim*" + f"{checksum('HB,1,0.1.0,5,sim'):02X}".encode() + b"\r\n$DI,1,1,0,1,0*10\n"))
        self.assertEqual([f.type for f in got], ["HB", "DI"])
        self.assertEqual(got[0].fields, ["1", "0.1.0", "5", "sim"])

    def test_reader_drops_oversized(self):
        r = Reader()
        self.assertEqual(list(r.feed(b"$" + b"A" * 200 + b"\n$DI,1,1,0,1,0*10\n")), [Frame("DI", list("11010"))])


if __name__ == "__main__":
    unittest.main()
