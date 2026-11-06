package dev.headhunt.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.headhunt.carkit.Gear
import dev.headhunt.carkit.VehicleState
import dev.headhunt.design.HeadhuntTheme
import dev.headhunt.design.Touch

/**
 * M0 home screen: proves the vehicle data path end to end.
 * The real launcher layout (map, media, app grid) lands in M1.
 */
@Composable
fun HomeScreen(state: VehicleState) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(Touch.Gutter),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "HEADHUNT",
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
        )

        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = state.speedMph?.toString() ?: "--",
                style = MaterialTheme.typography.displayLarge,
                color = colors.onBackground,
            )
            Text(
                text = "mph",
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurfaceVariant,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(Touch.Gutter)) {
            Readout("RPM", state.engineRpm?.toInt()?.toString())
            Readout("Coolant", state.coolantCelsius?.let { "${it.toInt()}°C" })
            Readout("Fuel", state.fuelLevel?.let { "${(it * 100).toInt()}%" })
            Readout("Gear", state.gear.takeIf { it != Gear.UNKNOWN }?.name)
            Readout("Mode", if (state.night) "Night" else "Day")
        }
    }
}

@Composable
private fun Readout(label: String, value: String?) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value ?: "--",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Preview(widthDp = 1024, heightDp = 600)
@Composable
private fun HomeScreenNightPreview() {
    HeadhuntTheme(night = true) {
        HomeScreen(
            VehicleState(
                speedMetersPerSecond = 29f,
                engineRpm = 3200f,
                coolantCelsius = 88f,
                fuelLevel = 0.62f,
                gear = Gear.DRIVE,
                night = true,
            )
        )
    }
}

@Preview(widthDp = 1024, heightDp = 600)
@Composable
private fun HomeScreenDayPreview() {
    HeadhuntTheme(night = false) {
        HomeScreen(VehicleState(night = false))
    }
}
