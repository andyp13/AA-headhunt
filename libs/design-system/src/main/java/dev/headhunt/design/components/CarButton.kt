package dev.headhunt.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.headhunt.design.Touch

/**
 * A large touch target with an icon and a short label.
 *
 * Every control the driver may use while moving goes through this so the
 * 76dp minimum from [Touch.MinTarget] is impossible to forget. [selected]
 * paints the accent so the current destination on a rail is obvious at a glance.
 */
@Composable
fun CarButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val tint = if (selected) colors.primary else colors.onSurface
    Column(
        modifier = modifier
            .defaultMinSize(minWidth = Touch.MinTarget, minHeight = Touch.MinTarget)
            .clip(RoundedCornerShape(Touch.CornerRadius))
            .background(if (selected) colors.surface else colors.background)
            .clickable(onClick = onClick, role = Role.Button)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.defaultMinSize(32.dp, 32.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = tint)
    }
}
