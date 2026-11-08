// Headhunt bridge: the microcontroller between the car and the head unit.
// See PROTOCOL.md for every frame this emits and accepts.
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "pico/stdlib.h"
#include "hardware/watchdog.h"
#include "config.h"
#include "frame.h"
#include "inputs.h"
#include "obd.h"
#include "pins.h"
#include "power.h"

#define PROTO_VERSION 1

static void send_di(const inputs_t *in) {
    frame_send("DI", "%d,%d,%d,%d,%d", in->acc, in->ign, in->rev, in->illum, in->pbrk);
}

static void send_hb(uint32_t now_ms) {
    frame_send("HB", "%d,%s,%lu,%s", PROTO_VERSION, FW_VERSION, (unsigned long)(now_ms / 1000),
               obd_sim() ? "sim" : "live");
}

static void handle(const frame_t *f, uint32_t now_ms) {
    if (!strcmp(f->type, "PING")) { send_hb(now_ms); return; }
    if (!strcmp(f->type, "BYE")) { power_on_bye(); return; }
    if (!strcmp(f->type, "HOLD") && f->nfields >= 1) { power_hold((uint32_t)strtoul(f->fields[0], NULL, 10)); return; }
    if (!strcmp(f->type, "SIM") && f->nfields >= 1) { obd_set_sim(!strcmp(f->fields[0], "on")); return; }
    if (!strcmp(f->type, "RST")) { frame_send("LOG", "I,rebooting"); sleep_ms(50); watchdog_reboot(0, 0, 0); return; }
    if (!strcmp(f->type, "CFG") && f->nfields >= 2) {
        if (config_set(f->fields[0], f->fields[1])) frame_send("CFG", "%s,%s", f->fields[0], f->fields[1]);
        else frame_send("LOG", "E,bad CFG %s", f->fields[0]);
        return;
    }
    if (!strcmp(f->type, "GET") && f->nfields >= 1) {
        char v[24];
        if (config_get(f->fields[0], v, sizeof v)) frame_send("CFG", "%s,%s", f->fields[0], v);
        else frame_send("LOG", "E,unknown key %s", f->fields[0]);
        return;
    }
    frame_send("LOG", "W,unknown frame %s", f->type);
}

int main(void) {
    stdio_init_all();
    gpio_init(PIN_LED); gpio_set_dir(PIN_LED, GPIO_OUT);
    config_load();
    inputs_init();
    power_init();
    obd_init();

    frame_t rx;
    inputs_t in = {0};
    uint32_t last_hb = 0, last_di = 0, last_bat = 0;
    float battery = 0.0f;

    for (;;) {
        uint32_t now = to_ms_since_boot(get_absolute_time());

        int c;
        while ((c = getchar_timeout_us(0)) != PICO_ERROR_TIMEOUT) {
            if (frame_feed((char)c, &rx)) handle(&rx, now);
        }

        bool changed = inputs_poll(now, &in);
        if (changed || now - last_di >= 500) { send_di(&in); last_di = now; }

        if (now - last_bat >= 1000) {
            battery = inputs_battery_volts();
            frame_send("BAT", "%.1f", (double)battery);
            last_bat = now;
        }

        power_state_t ps = power_tick(now, in.acc, battery);
        obd_tick(now, in.ign);

        if (now - last_hb >= 1000) { send_hb(now); last_hb = now; }
        gpio_put(PIN_LED, ps == PWR_ON ? (now / 500) % 2 : 1);  // slow blink when on, solid otherwise
        sleep_ms(5);
    }
}
