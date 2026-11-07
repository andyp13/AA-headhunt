#include "inputs.h"
#include "pins.h"
#include "hardware/adc.h"
#include "pico/stdlib.h"

#define DEBOUNCE_MS 50

static const uint8_t pins[5] = {PIN_IN_ACC, PIN_IN_IGN, PIN_IN_REV, PIN_IN_ILLUM, PIN_IN_PBRK};
static bool stable[5], last_raw[5];
static uint32_t changed_at[5];

void inputs_init(void) {
    for (int i = 0; i < 5; i++) {
        gpio_init(pins[i]);
        gpio_set_dir(pins[i], GPIO_IN);
        gpio_pull_down(pins[i]);
        stable[i] = last_raw[i] = gpio_get(pins[i]);
    }
    adc_init();
    adc_gpio_init(PIN_BAT_ADC);
    adc_select_input(0);
}

bool inputs_poll(uint32_t now_ms, inputs_t *out) {
    bool changed = false;
    for (int i = 0; i < 5; i++) {
        bool raw = gpio_get(pins[i]);
        if (raw != last_raw[i]) { last_raw[i] = raw; changed_at[i] = now_ms; }
        if (raw != stable[i] && now_ms - changed_at[i] >= DEBOUNCE_MS) { stable[i] = raw; changed = true; }
    }
    out->acc = stable[0]; out->ign = stable[1]; out->rev = stable[2];
    out->illum = stable[3]; out->pbrk = stable[4];
    return changed;
}

float inputs_battery_volts(void) {
    // 12-bit ADC, 3.3 V reference; average a few samples to tame ignition noise.
    uint32_t acc = 0;
    for (int i = 0; i < 8; i++) acc += adc_read();
    return (acc / 8.0f) * (3.3f / 4095.0f) * BAT_DIVIDER;
}
