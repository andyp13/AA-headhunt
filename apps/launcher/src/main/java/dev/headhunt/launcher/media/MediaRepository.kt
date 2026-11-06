package dev.headhunt.launcher.media

import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Mirrors the top active media session into [state].
 *
 * Needs android.permission.MEDIA_CONTENT_CONTROL, a signature permission, so
 * this only works when the launcher is platform-signed. Without it the card
 * stays in its empty state and we log once instead of crashing.
 */
class MediaRepository(private val context: Context) : MediaControls, AutoCloseable {

    private val manager = context.getSystemService(MediaSessionManager::class.java)
    private val _state = MutableStateFlow(MediaState())
    val state: StateFlow<MediaState> = _state

    private var controller: MediaController? = null

    private val controllerCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) = refresh()
        override fun onPlaybackStateChanged(state: PlaybackState?) = refresh()
        override fun onSessionDestroyed() = bind(emptyList())
    }

    private val sessionsListener = MediaSessionManager.OnActiveSessionsChangedListener { bind(it.orEmpty()) }

    init {
        runCatching {
            manager.addOnActiveSessionsChangedListener(sessionsListener, null)
            bind(manager.getActiveSessions(null))
        }.onFailure { Log.w(TAG, "media sessions unavailable (missing MEDIA_CONTENT_CONTROL?)", it) }
    }

    private fun bind(controllers: List<MediaController>) {
        val next = controllers.firstOrNull()
        if (next?.sessionToken == controller?.sessionToken) {
            refresh(); return
        }
        controller?.unregisterCallback(controllerCallback)
        controller = next
        next?.registerCallback(controllerCallback)
        refresh()
    }

    private fun refresh() {
        val c = controller
        if (c == null) {
            _state.value = MediaState(); return
        }
        val meta = c.metadata
        _state.value = MediaState(
            available = true,
            appName = appLabel(c.packageName),
            title = meta?.getString(MediaMetadata.METADATA_KEY_TITLE),
            artist = meta?.getString(MediaMetadata.METADATA_KEY_ARTIST)
                ?: meta?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST),
            playing = c.playbackState?.state == PlaybackState.STATE_PLAYING,
        )
    }

    private fun appLabel(packageName: String): String = runCatching {
        val pm = context.packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
    }.getOrDefault(packageName)

    override fun playPause() {
        val c = controller ?: return
        if (c.playbackState?.state == PlaybackState.STATE_PLAYING) c.transportControls.pause()
        else c.transportControls.play()
    }

    override fun next() { controller?.transportControls?.skipToNext() }
    override fun previous() { controller?.transportControls?.skipToPrevious() }

    override fun close() {
        runCatching { manager.removeOnActiveSessionsChangedListener(sessionsListener) }
        controller?.unregisterCallback(controllerCallback)
        controller = null
    }

    private companion object {
        const val TAG = "MediaRepository"
    }
}
