package dev.headhunt.carkit

import kotlinx.coroutines.flow.StateFlow

/**
 * The single entry point apps use for vehicle data.
 *
 * Apps never touch CarPropertyManager directly. They take a [VehicleSource],
 * which lets the emulator, the bench rig and unit tests swap in a
 * [FakeVehicleSource] without the UI knowing.
 */
interface VehicleSource : AutoCloseable {
    val state: StateFlow<VehicleState>
}
