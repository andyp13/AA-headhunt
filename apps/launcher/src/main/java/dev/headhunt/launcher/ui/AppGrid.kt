package dev.headhunt.launcher.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import dev.headhunt.design.Touch
import dev.headhunt.launcher.apps.AppEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AppGrid(
    loadApps: () -> List<AppEntry>,
    onLaunch: (AppEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    val apps by produceState(initialValue = emptyList<AppEntry>()) {
        value = withContext(Dispatchers.IO) { loadApps() }
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(end = Touch.Gutter, top = Touch.Gutter, bottom = Touch.Gutter),
        horizontalArrangement = Arrangement.spacedBy(Touch.Gap),
        verticalArrangement = Arrangement.spacedBy(Touch.Gap),
    ) {
        items(apps, key = { it.launchIntent.component?.flattenToString() ?: it.packageName }) { app ->
            AppTile(app, onClick = { onLaunch(app) })
        }
    }
}

@Composable
private fun AppTile(app: AppEntry, onClick: () -> Unit) {
    val bitmap = remember(app.packageName) { app.icon.toBitmap(ICON_PX, ICON_PX).asImageBitmap() }
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(Touch.Gap / 2),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(bitmap, contentDescription = null, modifier = Modifier.size(80.dp))
        Text(
            app.label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

private const val ICON_PX = 192
