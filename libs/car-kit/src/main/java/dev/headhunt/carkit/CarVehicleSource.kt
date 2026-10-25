package dev.headhunt.carkit

import android.car.Car
import android.car.VehicleGear
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Real implementation backed by Car Service.
 *
 * Works unchanged on the AAOS emulator (Google's reference VHAL) and later on
 * the Eclipse once platform/vhal publishes the same property IDs from the
 * bridge. That is the whole point of going through standard VehicleProperties.
 */
class CarVehicleSource(context: Context) : VehicleSource {

    private val _state = MutableStateFlow(VehicleState())
    override val state: StateFlow<VehicleState> = _state

    private val car: Car = Car.createCar(context.applicationContext)
    private val props = car.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager

    private val callback = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(value: CarPropertyValue<*>) {
            _state.update { s ->
                when (value.propertyId) {
                    VehiclePropertyIds.PERF_VEHICLE_SPEED ->
                        s.copy(speedMetersPerSecond = value.value as? Float)
                    VehiclePropertyIds.ENGINE_RPM ->
                        s.copy(engineRpm = value.value as? Float)
                    VehiclePropertyIds.ENGINE_COOLANT_TEMP ->
                        s.copy(coolantCelsius = value.value as? Float)
                    VehiclePropertyIds.FUEL_LEVEL ->
                        s.copy(fuelLevel = (value.value as? Float)?.let { ml -> fuelFraction(ml) })
                    VehiclePropertyIds.GEAR_SELECTION ->
                        s.copy(gear = gearOf(value.value as? Int))
                    VehiclePropertyIds.NIGHT_MODE ->
                        s.copy(night = value.value as? Boolean ?: s.night)
                    else -> s
                }
            }
        }

        override fun onErrorEvent(propId: Int, zone: Int) {
            Log.w(TAG, "property error id=0x${propId.toString(16)} zone=$zone")
        }
    }

    private var tankCapacityMl: Float? = null

    init {
        tankCapacityMl = runCatching {
            props.getFloatProperty(VehiclePropertyIds.INFO_FUEL_CAPACITY, 0)
        }.getOrNull()

        SUBSCRIPTIONS.forEach { (id, rate) ->
            val ok = runCatching { props.registerCallback(callback, id, rate) }.getOrDefault(false)
            if (!ok) Log.w(TAG, "could not subscribe to 0x${id.toString(16)} (missing permission or unsupported)")
        }
    }

    private fun fuelFraction(ml: Float): Float? =
        tankCapacityMl?.takeIf { it > 0f }?.let { (ml / it).coerceIn(0f, 1f) }

    private fun gearOf(raw: Int?): Gear = when (raw) {
        VehicleGear.GEAR_PARK -> Gear.PARK
        VehicleGear.GEAR_REVERSE -> Gear.REVERSE
        VehicleGear.GEAR_NEUTRAL -> Gear.NEUTRAL
        null -> Gear.UNKNOWN
        else -> Gear.DRIVE
    }

    override fun close() {
        runCatching { props.unregisterCallback(callback) }
        car.disconnect()
    }

    private companion object {
        const val TAG = "CarVehicleSource"

        /** Property id to sample rate in Hz. Speed and RPM are the only fast ones. */
        val SUBSCRIPTIONS = listOf(
            VehiclePropertyIds.PERF_VEHICLE_SPEED to CarPropertyManager.SENSOR_RATE_UI,
            VehiclePropertyIds.ENGINE_RPM to CarPropertyManager.SENSOR_RATE_UI,
            VehiclePropertyIds.ENGINE_COOLANT_TEMP to CarPropertyManager.SENSOR_RATE_NORMAL,
            VehiclePropertyIds.FUEL_LEVEL to CarPropertyManager.SENSOR_RATE_ONCHANGE,
            VehiclePropertyIds.GEAR_SELECTION to CarPropertyManager.SENSOR_RATE_ONCHANGE,
            VehiclePropertyIds.NIGHT_MODE to CarPropertyManager.SENSOR_RATE_ONCHANGE,
        )
    }
}
