#include "config.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

config_t g_config;

void config_load(void) {
    // TODO(M3): persist in the last flash sector; defaults until then.
    g_config.keyoff_delay_s = 30;
    g_config.shutdown_timeout_s = 20;
    g_config.lowbat_cut_v = 11.8f;
}

bool config_set(const char *key, const char *value) {
    char *end;
    if (!strcmp(key, "keyoff_delay_s")) {
        unsigned long v = strtoul(value, &end, 10);
        if (*end || v > 3600) return false;
        g_config.keyoff_delay_s = (uint32_t)v; return true;
    }
    if (!strcmp(key, "shutdown_timeout_s")) {
        unsigned long v = strtoul(value, &end, 10);
        if (*end || v < 5 || v > 600) return false;
        g_config.shutdown_timeout_s = (uint32_t)v; return true;
    }
    if (!strcmp(key, "lowbat_cut_v")) {
        float v = strtof(value, &end);
        if (*end || v < 9.0f || v > 13.0f) return false;
        g_config.lowbat_cut_v = v; return true;
    }
    return false;
}

bool config_get(const char *key, char *out, int outlen) {
    if (!strcmp(key, "keyoff_delay_s")) { snprintf(out, outlen, "%lu", (unsigned long)g_config.keyoff_delay_s); return true; }
    if (!strcmp(key, "shutdown_timeout_s")) { snprintf(out, outlen, "%lu", (unsigned long)g_config.shutdown_timeout_s); return true; }
    if (!strcmp(key, "lowbat_cut_v")) { snprintf(out, outlen, "%.1f", (double)g_config.lowbat_cut_v); return true; }
    return false;
}
