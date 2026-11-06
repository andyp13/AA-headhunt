package dev.headhunt.launcher.media

/** What the media card shows. All fields null until a session exists. */
data class MediaState(
    val available: Boolean = false,
    val appName: String? = null,
    val title: String? = null,
    val artist: String? = null,
    val playing: Boolean = false,
) {
    /** "Title · Artist", or whichever half exists, or null. */
    fun trackLine(): String? = listOfNotNull(
        title?.takeIf { it.isNotBlank() },
        artist?.takeIf { it.isNotBlank() },
    ).joinToString(" · ").ifEmpty { null }
}

/** Transport controls the UI may call. [MediaRepository] implements it. */
interface MediaControls {
    fun playPause()
    fun next()
    fun previous()
}
