#pragma once
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>

// PROTOCOL.md framing: $TYPE,f1,f2*HH\n with an NMEA XOR checksum.
#define FRAME_MAX 96
#define FRAME_MAX_FIELDS 8

typedef struct {
    char type[5];
    int nfields;
    const char *fields[FRAME_MAX_FIELDS];
    char buf[FRAME_MAX];
} frame_t;

// Emit a frame to stdout (USB CDC). fmt is the comma-separated body after the type.
void frame_send(const char *type, const char *fmt, ...) __attribute__((format(printf, 2, 3)));

// Feed one received byte. Returns true when a complete, checksum-valid frame
// is in *out. Invalid or oversized frames are dropped silently.
bool frame_feed(char c, frame_t *out);

uint8_t frame_checksum(const char *body, size_t len);
