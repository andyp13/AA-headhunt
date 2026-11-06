package dev.headhunt.carkit

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/** Scriptable source for previews, tests and the emulator without Car Service. */
class FakeVehicleSource(initial: VehicleState = VehicleState()) : VehicleSource {
    private val _state = MutableStateFlow(initial)
    override val state: StateFlow<VehicleState> = _state

    fun set(transform: VehicleState.() -> VehicleState) = _state.update(transform)

    override fun close() = Unit
}
