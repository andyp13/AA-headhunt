#pragma once
#include <stdbool.h>
#include <stdint.h>

typedef enum { PWR_ON, PWR_KEYOFF, PWR_SHUTDOWN, PWR_CUT } power_state_t;

void power_init(void);
// Advance the state machine. Emits PWR frames on change and once a second
// while not ON. Returns the current state.
power_state_t power_tick(uint32_t now_ms, bool acc, float battery_v);
void power_on_bye(void);               // Android finished shutting down
void power_hold(uint32_t seconds);     // postpone the cut
const char *power_state_name(power_state_t s);
