cmake_minimum_required(VERSION 3.13)

# Headhunt bridge firmware for Raspberry Pi Pico (RP2040).
# Build:  cmake -B build -DPICO_SDK_PATH=/path/to/pico-sdk && cmake --build build
# Flash:  hold BOOTSEL, plug in, copy build/headhunt_bridge.uf2 to the RPI-RP2 drive.
set(PICO_BOARD pico CACHE STRING "Board type")
include(pico_sdk_import.cmake)

project(headhunt_bridge C CXX ASM)
set(CMAKE_C_STANDARD 11)
pico_sdk_init()

add_executable(headhunt_bridge
  src/main.c
  src/frame.c
  src/inputs.c
  src/power.c
  src/obd.c
  src/config.c
)
target_include_directories(headhunt_bridge PRIVATE src)
target_compile_options(headhunt_bridge PRIVATE -Wall -Wextra -Werror)
if(NOT FW_VERSION)
  set(FW_VERSION "0.1.0-dev")
endif()
target_compile_definitions(headhunt_bridge PRIVATE FW_VERSION="${FW_VERSION}")

target_link_libraries(headhunt_bridge pico_stdlib hardware_adc hardware_watchdog)

# USB CDC is the only link to Android. No UART stdio: UART0 is the K-line.
pico_enable_stdio_usb(headhunt_bridge 1)
pico_enable_stdio_uart(headhunt_bridge 0)
pico_add_extra_outputs(headhunt_bridge)
