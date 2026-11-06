package dev.headhunt.launcher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import dev.headhunt.design.Touch
import dev.headhunt.design.components.CarButton
import dev.headhunt.launcher.R

@Composable
fun NavRail(
    current: Destination,
    onHome: () -> Unit,
    onApps: () -> Unit,
    onMedia: () -> Unit,
    onPhone: () -> Unit,
    onSettings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(112.dp)
            .padding(vertical = Touch.Gutter, horizontal = Touch.Gap / 2),
        verticalArrangement = Arrangement.spacedBy(Touch.Gap / 2),
    ) {
        CarButton(Icons.Filled.Home, stringResource(R.string.nav_home), onHome, selected = current == Destination.HOME)
        CarButton(vector(R.drawable.ic_apps), stringResource(R.string.nav_apps), onApps, selected = current == Destination.APPS)
        CarButton(vector(R.drawable.ic_music), stringResource(R.string.nav_media), onMedia)
        CarButton(Icons.Filled.Phone, stringResource(R.string.nav_phone), onPhone)
        CarButton(Icons.Filled.Settings, stringResource(R.string.nav_settings), onSettings)
    }
}

@Composable
private fun vector(id: Int): ImageVector = ImageVector.vectorResource(id)
