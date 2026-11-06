package dev.headhunt.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Design tokens for the head unit.
 *
 * Everything the driver touches while moving must respect [Touch]. The values
 * come from the AAOS distraction guidelines: a 76dp minimum target and a type
 * scale that stays legible at arm's length on a 7-8 inch panel.
 */
object HeadhuntColors {
    // Night palette is the default. A car cabin is dark more often than bright.
    val NightBackground = Color(0xFF0B0D10)
    val NightSurface = Color(0xFF161A1F)
    val NightOnSurface = Color(0xFFE9EDF1)
    val NightMuted = Color(0xFF8A94A0)

    val DayBackground = Color(0xFFF4F6F8)
    val DaySurface = Color(0xFFFFFFFF)
    val DayOnSurface = Color(0xFF14181C)
    val DayMuted = Color(0xFF5B6670)

    val Accent = Color(0xFFFF6A00)
    val Warning = Color(0xFFFFC400)
    val Danger = Color(0xFFFF3B30)
}

object Touch {
    /** Minimum touch target for any control usable while driving. */
    val MinTarget = 76.dp
    val Gutter = 24.dp
    val Gap = 16.dp
    val CornerRadius = 16.dp
}

object Type {
    val Hero = 96.sp      // primary speed readout
    val Display = 48.sp   // card headlines
    val Title = 32.sp
    val Body = 24.sp
    val Caption = 18.sp
}
