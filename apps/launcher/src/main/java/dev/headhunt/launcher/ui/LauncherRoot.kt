package dev.headhunt.launcher.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.headhunt.carkit.VehicleState
import dev.headhunt.launcher.apps.AppEntry
import dev.headhunt.launcher.media.MediaControls
import dev.headhunt.launcher.media.MediaState

enum class Destination { HOME, APPS }

/**
 * Top-level layout: a nav rail on the left and the current destination
 * filling the rest. Only HOME and APPS are screens of ours; the other rail
 * buttons hand off to whatever app owns that job.
 */
@Composable
fun LauncherRoot(
    vehicle: VehicleState,
    media: MediaState,
    mediaControls: MediaControls,
    loadApps: () -> List<AppEntry>,
    launch: (Intent) -> Unit,
) {
    var destination by rememberSaveable { mutableStateOf(Destination.HOME) }

    Row(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        NavRail(
            current = destination,
            onHome = { destination = Destination.HOME },
            onApps = { destination = Destination.APPS },
            onMedia = { launch(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MUSIC)) },
            onPhone = { launch(Intent(Intent.ACTION_DIAL)) },
            onSettings = { launch(Intent(Settings.ACTION_SETTINGS)) },
        )
        when (destination) {
            Destination.HOME -> HomeScreen(
                vehicle = vehicle,
                media = media,
                mediaControls = mediaControls,
                modifier = Modifier.weight(1f),
            )
            Destination.APPS -> AppGrid(
                loadApps = loadApps,
                onLaunch = { launch(it.launchIntent) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
