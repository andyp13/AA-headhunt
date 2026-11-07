#include "power.h"
#include "config.h"
#include "frame.h"
#include "pins.h"
#include "pico/stdlib.h"

#define CUT_GRACE_MS 3000

static power_state_t state = PWR_ON;
static uint32_t deadline_ms;      // when the current state times out
static uint32_t last_report_ms;
static bool bye_received;

const char *power_state_name(power_state_t s) {
    switch (s) {
        case PWR_ON: return "on";
        case PWR_KEYOFF: return "keyoff";
        case PWR_SHUTDOWN: return "shutdown";
        default: return "cut";
    }
}

static void report(uint32_t now_ms) {
    uint32_t left = (state == PWR_ON || now_ms >= deadline_ms) ? 0 : (deadline_ms - now_ms + 999) / 1000;
    frame_send("PWR", "%s,%lu", power_state_name(state), (unsigned long)left);
    last_report_ms = now_ms;
}

static void enter(power_state_t s, uint32_t now_ms, uint32_t duration_ms) {
    state = s;
    deadline_ms = now_ms + duration_ms;
    report(now_ms);
}

void power_init(void) {
    gpio_init(PIN_PWR_HOLD);
    gpio_set_dir(PIN_PWR_HOLD, GPIO_OUT);
    gpio_put(PIN_PWR_HOLD, 1);   // we are running, so ACC was on: hold the rail
    state = PWR_ON;
}

void power_on_bye(void) { if (state == PWR_SHUTDOWN) bye_received = true; }

void power_hold(uint32_t seconds) {
    if (seconds > 600) seconds = 600;
    if (state == PWR_KEYOFF || state == PWR_SHUTDOWN) deadline_ms += seconds * 1000;
}

power_state_t power_tick(uint32_t now_ms, bool acc, float battery_v) {
    switch (state) {
        case PWR_ON:
            if (!acc) enter(PWR_KEYOFF, now_ms, g_config.keyoff_delay_s * 1000);
            break;
        case PWR_KEYOFF:
            if (acc) { enter(PWR_ON, now_ms, 0); break; }
            if (battery_v > 0.5f && battery_v < g_config.lowbat_cut_v) {
                frame_send("LOG", "W,low battery %.1fV: early shutdown", (double)battery_v);
                enter(PWR_SHUTDOWN, now_ms, 5000);
            } else if (now_ms >= deadline_ms) {
                bye_received = false;
                enter(PWR_SHUTDOWN, now_ms, g_config.shutdown_timeout_s * 1000);
            }
            break;
        case PWR_SHUTDOWN:
            // ACC returning here is too late to abort cleanly; Android is already going down.
            // It will boot again when the rail comes back.
            if (bye_received || now_ms >= deadline_ms) enter(PWR_CUT, now_ms, CUT_GRACE_MS);
            break;
        case PWR_CUT:
            if (now_ms >= deadline_ms) {
                gpio_put(PIN_PWR_HOLD, 0);  // rail off; if ACC is high this also resets us
                sleep_ms(500);
                if (acc) { gpio_put(PIN_PWR_HOLD, 1); enter(PWR_ON, now_ms, 0); }
            }
            break;
    }
    if (state != PWR_ON && now_ms - last_report_ms >= 1000) report(now_ms);
    return state;
}
