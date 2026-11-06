package dev.headhunt.carkit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VehicleStateTest {
    @Test
    fun `speed converts from meters per second`() {
        val s = VehicleState(speedMetersPerSecond = 27.0f) // ~60 mph
        assertEquals(60, s.speedMph)
        assertEquals(97, s.speedKph)
    }

    @Test
    fun `unknown speed stays null`() {
        assertNull(VehicleState().speedMph)
    }

    @Test
    fun `fake source publishes updates`() {
        val fake = FakeVehicleSource()
        fake.set { copy(gear = Gear.REVERSE, night = false) }
        assertEquals(Gear.REVERSE, fake.state.value.gear)
        assertEquals(false, fake.state.value.night)
    }
}
