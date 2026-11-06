package dev.headhunt.launcher.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MediaStateTest {
    @Test
    fun `track line joins title and artist`() {
        assertEquals("Dreams · Fleetwood Mac", MediaState(title = "Dreams", artist = "Fleetwood Mac").trackLine())
    }

    @Test
    fun `track line tolerates a missing half`() {
        assertEquals("Dreams", MediaState(title = "Dreams").trackLine())
        assertEquals("Fleetwood Mac", MediaState(title = " ", artist = "Fleetwood Mac").trackLine())
    }

    @Test
    fun `track line is null when nothing is known`() {
        assertNull(MediaState().trackLine())
    }
}
