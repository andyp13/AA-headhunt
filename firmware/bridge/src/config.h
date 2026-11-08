#pragma once
#include <stdbool.h>
#include <stdint.h>

typedef struct {
    uint32_t keyoff_delay_s;      // ACC low -> shutdown request
    uint32_t shutdown_timeout_s;  // shutdown request -> cut, absent BYE
    float lowbat_cut_v;           // cut early below this while in keyoff
} config_t;

extern config_t g_config;

void config_load(void);
// Returns false for an unknown key or unparsable value.
bool config_set(const char *key, const char *value);
// Formats the value for key into out; returns false for an unknown key.
bool config_get(const char *key, char *out, int outlen);
