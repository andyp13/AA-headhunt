package dev.headhunt.launcher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.headhunt.design.Touch
import dev.headhunt.design.components.CarButton
import dev.headhunt.design.components.Panel
import dev.headhunt.launcher.R
import dev.headhunt.launcher.media.MediaControls
import dev.headhunt.launcher.media.MediaState

@Composable
fun MediaCard(media: MediaState, controls: MediaControls, modifier: Modifier = Modifier) {
    Panel(modifier, title = media.appName ?: stringResource(R.string.panel_media)) {
        if (!media.available) {
            Text(
                stringResource(R.string.media_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Panel
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                media.title ?: stringResource(R.string.media_unknown_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                media.artist ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Touch.Gap / 2)) {
            CarButton(ImageVector.vectorResource(R.drawable.ic_skip_previous), stringResource(R.string.media_previous), controls::previous, Modifier.weight(1f))
            CarButton(
                if (media.playing) ImageVector.vectorResource(R.drawable.ic_pause) else Icons.Filled.PlayArrow,
                stringResource(if (media.playing) R.string.media_pause else R.string.media_play),
                controls::playPause,
                Modifier.weight(1f),
                selected = true,
            )
            CarButton(ImageVector.vectorResource(R.drawable.ic_skip_next), stringResource(R.string.media_next), controls::next, Modifier.weight(1f))
        }
    }
}

