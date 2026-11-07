#pragma once
#include <stdbool.h>
#include <stdint.h>

// Diagnostic-port side. Real ISO 9141-2 / KWP2000 arrives in M2 once the
// transceiver is on the bench; until then this is the interface plus a
// simulator that produces plausible numbers for the UI.
void obd_init(void);
void obd_set_sim(bool on);
bool obd_sim(void);
// Poll at ~5 Hz. Emits SPD/RPM/ECT/FUEL frames as values arrive.
void obd_tick(uint32_t now_ms, bool ign);
