#include "frame.h"
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

uint8_t frame_checksum(const char *body, size_t len) {
    uint8_t x = 0;
    for (size_t i = 0; i < len; i++) x ^= (uint8_t)body[i];
    return x;
}

void frame_send(const char *type, const char *fmt, ...) {
    char body[FRAME_MAX];
    int n = snprintf(body, sizeof body, "%s", type);
    if (fmt && *fmt) {
        body[n++] = ',';
        va_list ap;
        va_start(ap, fmt);
        n += vsnprintf(body + n, sizeof body - n, fmt, ap);
        va_end(ap);
    }
    if (n < 0 || n >= (int)sizeof body - 4) return;  // never emit a truncated frame
    printf("$%s*%02X\n", body, frame_checksum(body, (size_t)n));
}

static char rx[FRAME_MAX];
static int rxn = -1;  // -1: waiting for '$'

static bool parse(char *line, frame_t *out) {
    char *star = strrchr(line, '*');
    if (!star || strlen(star) != 3) return false;
    uint8_t want = (uint8_t)strtoul(star + 1, NULL, 16);
    if (frame_checksum(line, (size_t)(star - line)) != want) return false;
    *star = '\0';

    out->nfields = 0;
    char *p = line;
    char *comma = strchr(p, ',');
    size_t tlen = comma ? (size_t)(comma - p) : strlen(p);
    if (tlen < 2 || tlen > 4) return false;
    memcpy(out->type, p, tlen);
    out->type[tlen] = '\0';
    while (comma && out->nfields < FRAME_MAX_FIELDS) {
        p = comma + 1;
        comma = strchr(p, ',');
        if (comma) *comma = '\0';
        out->fields[out->nfields++] = p;
    }
    return true;
}

bool frame_feed(char c, frame_t *out) {
    if (c == '$') { rxn = 0; return false; }
    if (rxn < 0) return false;
    if (c == '\r') return false;
    if (c == '\n') {
        rx[rxn] = '\0';
        memcpy(out->buf, rx, (size_t)rxn + 1);
        rxn = -1;
        return parse(out->buf, out);
    }
    if (rxn >= FRAME_MAX - 1) { rxn = -1; return false; }
    rx[rxn++] = c;
    return false;
}
