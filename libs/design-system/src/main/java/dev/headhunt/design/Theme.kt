package dev.headhunt.design

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

private val NightScheme: ColorScheme = darkColorScheme(
    background = HeadhuntColors.NightBackground,
    surface = HeadhuntColors.NightSurface,
    onBackground = HeadhuntColors.NightOnSurface,
    onSurface = HeadhuntColors.NightOnSurface,
    onSurfaceVariant = HeadhuntColors.NightMuted,
    primary = HeadhuntColors.Accent,
    error = HeadhuntColors.Danger,
)

private val DayScheme: ColorScheme = lightColorScheme(
    background = HeadhuntColors.DayBackground,
    surface = HeadhuntColors.DaySurface,
    onBackground = HeadhuntColors.DayOnSurface,
    onSurface = HeadhuntColors.DayOnSurface,
    onSurfaceVariant = HeadhuntColors.DayMuted,
    primary = HeadhuntColors.Accent,
    error = HeadhuntColors.Danger,
)

private val HeadhuntTypography = Typography(
    displayLarge = TextStyle(fontSize = Type.Hero, fontWeight = FontWeight.SemiBold),
    displayMedium = TextStyle(fontSize = Type.Display, fontWeight = FontWeight.Medium),
    titleLarge = TextStyle(fontSize = Type.Title, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = Type.Body),
    labelMedium = TextStyle(fontSize = Type.Caption),
)

/**
 * Root theme. [night] should be driven by the vehicle's NIGHT_MODE property,
 * which on the Eclipse will come from the dash illumination line via the bridge.
 */
@Composable
fun HeadhuntTheme(night: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (night) NightScheme else DayScheme,
        typography = HeadhuntTypography,
        content = content,
    )
}
