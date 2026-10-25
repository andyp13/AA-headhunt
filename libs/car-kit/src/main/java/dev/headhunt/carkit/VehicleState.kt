package dev.headhunt.carkit

/**
 * The slice of vehicle state the UI layer is allowed to see.
 *
 * Units are SI, matching the VehicleProperty definitions, so the UI converts
 * for display. On the Eclipse these fields map to:
 *  - speed / rpm / coolant: OBD-II PIDs over ISO 9141-2
 *  - fuelLevel: OBD PID 0x2F if the ECU reports it, else the sender wire
 *  - gear: the reverse-light line via the bridge (REVERSE or DRIVE only)
 *  - night: the dash illumination line via the bridge
 */
data class VehicleState(
    val speedMetersPerSecond: Float? = null,
    val engineRpm: Float? = null,
    val coolantCelsius: Float? = null,
    /** 0f..1f, or null when unknown. */
    val fuelLevel: Float? = null,
    val gear: Gear = Gear.UNKNOWN,
    val night: Boolean = true,
) {
    val speedMph: Int? get() = speedMetersPerSecond?.let { (it * 2.23694f).toInt() }
    val speedKph: Int? get() = speedMetersPerSecond?.let { (it * 3.6f).toInt() }
}

enum class Gear { UNKNOWN, PARK, REVERSE, NEUTRAL, DRIVE }
