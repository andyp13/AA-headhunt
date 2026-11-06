package dev.headhunt.launcher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.headhunt.carkit.Gear
import dev.headhunt.carkit.VehicleState
import dev.headhunt.design.HeadhuntTheme
import dev.headhunt.design.Touch
import dev.headhunt.design.components.Panel
import dev.headhunt.design.components.Readout
import dev.headhunt.launcher.R
import dev.headhunt.launcher.media.MediaControls
import dev.headhunt.launcher.media.MediaState

/**
 * Home: map on the left, media and vehicle on the right, status strip below.
 * The map is a placeholder until M4 decides which navigation app the kit ships.
 */
@Composable
fun HomeScreen(
    vehicle: VehicleState,
    media: MediaState,
    mediaControls: MediaControls,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(end = Touch.Gutter, top = Touch.Gutter, bottom = Touch.Gutter),
        horizontalArrangement = Arrangement.spacedBy(Touch.Gap),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Touch.Gap)) {
            MapPlaceholder(Modifier.weight(1f).fillMaxWidth())
            CarStatusBar(vehicle)
        }
        Column(Modifier.width(360.dp), verticalArrangement = Arrangement.spacedBy(Touch.Gap)) {
            SpeedCard(vehicle)
            MediaCard(media, mediaControls, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MapPlaceholder(modifier: Modifier) {
    Panel(modifier, title = stringResource(R.string.panel_navigation)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(72.dp),
                )
                Text(
                    stringResource(R.string.map_placeholder),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SpeedCard(vehicle: VehicleState) {
    Panel(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = vehicle.speedMph?.toString() ?: "--",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.unit_mph),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
    }
}

@Composable
private fun CarStatusBar(vehicle: VehicleState) {
    Panel(Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(Touch.Gutter)) {
            Readout(stringResource(R.string.readout_gear), vehicle.gear.takeIf { it != Gear.UNKNOWN }?.name?.take(1))
            Readout(stringResource(R.string.readout_rpm), vehicle.engineRpm?.toInt()?.toString())
            Readout(stringResource(R.string.readout_coolant), vehicle.coolantCelsius?.let { "${it.toInt()}°" })
            Readout(stringResource(R.string.readout_fuel), vehicle.fuelLevel?.let { "${(it * 100).toInt()}%" })
            Readout(stringResource(R.string.readout_mode), stringResource(if (vehicle.night) R.string.mode_night else R.string.mode_day))
        }
    }
}

private object PreviewControls : MediaControls {
    override fun playPause() = Unit
    override fun next() = Unit
    override fun previous() = Unit
}

@Preview(widthDp = 1024, heightDp = 600)
@Composable
private fun HomeNightPreview() {
    HeadhuntTheme(night = true) {
        HomeScreen(
            vehicle = VehicleState(speedMetersPerSecond = 29f, engineRpm = 3200f, coolantCelsius = 88f, fuelLevel = 0.62f, gear = Gear.DRIVE),
            media = MediaState(available = true, appName = "Music", title = "Dreams", artist = "Fleetwood Mac", playing = true),
            mediaControls = PreviewControls,
        )
    }
}

@Preview(widthDp = 1024, heightDp = 600)
@Composable
private fun HomeDayEmptyPreview() {
    HeadhuntTheme(night = false) {
        HomeScreen(vehicle = VehicleState(night = false), media = MediaState(), mediaControls = PreviewControls)
    }
}
