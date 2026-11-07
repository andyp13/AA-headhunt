#include "obd.h"
#include "frame.h"
#include <math.h>

static bool sim = false;
static uint32_t last_poll_ms, last_slow_ms;
static bool reported_status;

void obd_init(void) { reported_status = false; }
void obd_set_sim(bool on) { sim = on; reported_status = false; }
bool obd_sim(void) { return sim; }

static void sim_tick(uint32_t now_ms) {
    float t = now_ms / 1000.0f;
    // A gentle drive cycle: 0..100 km/h over a minute, idling between.
    float kph = fmaxf(0.0f, 50.0f + 50.0f * sinf(t / 10.0f));
    float rpm = 850.0f + kph * 32.0f;
    frame_send("SPD", "%.1f,obd", (double)kph);
    frame_send("RPM", "%d", (int)rpm);
    if (now_ms - last_slow_ms >= 2000) {
        last_slow_ms = now_ms;
        frame_send("ECT", "%d", (int)fminf(90.0f, 20.0f + t / 3.0f));
        frame_send("FUEL", "%d", (int)fmaxf(5.0f, 62.0f - t / 120.0f));
    }
}

void obd_tick(uint32_t now_ms, bool ign) {
    if (!reported_status) {
        frame_send("OBD", "%s,%s", sim ? "ok" : "off", sim ? "SIM" : "");
        reported_status = true;
    }
    if (now_ms - last_poll_ms < 200) return;
    last_poll_ms = now_ms;
    if (sim) { sim_tick(now_ms); return; }
    (void)ign;
    // TODO(M2): ISO 9141-2 5-baud init on KLINE_UART, then PIDs 0x0D 0x0C 0x05 0x2F round-robin.
}
