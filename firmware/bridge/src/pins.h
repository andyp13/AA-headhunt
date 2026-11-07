#pragma once
// Pico pin map for the bench rig (hardware/bench-rig.md). All inputs are
// 3.3 V logic after the conditioning stage; active high.
#define PIN_IN_ACC     2
#define PIN_IN_IGN     3
#define PIN_IN_REV     4
#define PIN_IN_ILLUM   5
#define PIN_IN_PBRK    6
#define PIN_IN_VSS     7    // speed pulse, optional
#define PIN_PWR_HOLD   15   // high keeps the head unit's 5 V rail on
#define PIN_LED        25
#define PIN_BAT_ADC    26   // ADC0, battery through a divider
#define BAT_DIVIDER    5.70f  // (R1+R2)/R2 for a 47k/10k divider: 16.8 V full scale
#define KLINE_UART     uart0  // GPIO 0/1 to the ISO 9141 transceiver
