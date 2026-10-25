# ADR 0003: Compute board

Status: Proposed. Decide by end of M2 with bench data.

## Options

| Board | For | Against |
|-------|-----|---------|
| Raspberry Pi 5 | Most active community AAOS port. GPIO for the bench. Cheap. Everyone has one. | Not automotive-grade. x86-style boot time. No MIPI-DSI touch panels off the shelf in head-unit sizes. |
| RK3588 SBC (Radxa Rock 5B, Orange Pi 5 class) | Rockchip ships an Android BSP. Fast. HDMI, DSI, PCIe, plenty of USB. Wide temperature variants exist. | Smaller AAOS community. BSP quality varies by vendor. |
| Snapdragon automotive dev kit (SA8155/SA8295) | The real thing. Fast boot, automotive power management, safety islands. | Cost. NDA-gated BSPs. Overkill for a retrofit kit. |

## Proposal

Develop on a Pi 5 for M2 because it gets to a booting AAOS fastest. Carry an
RK3588 board in parallel as the production candidate. Pick at M3 based on
measured cold boot, resume-from-suspend, idle power draw and thermal
behaviour on a bench PSU.

## Not decided here

Display panel and touch controller. These follow the board, since DSI versus
HDMI decides the panel.
