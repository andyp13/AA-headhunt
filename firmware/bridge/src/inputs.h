#pragma once
#include <stdbool.h>
#include <stdint.h>

typedef struct {
    bool acc, ign, rev, illum, pbrk;
} inputs_t;

void inputs_init(void);
// Debounced sample; returns true when any line changed since the last call.
bool inputs_poll(uint32_t now_ms, inputs_t *out);
float inputs_battery_volts(void);
