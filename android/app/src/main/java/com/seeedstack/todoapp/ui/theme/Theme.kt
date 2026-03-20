package com.seeedstack.todoapp.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BgColor = Color(0xFF0F1117)
val SurfaceColor = Color(0xFF1A1D27)
val BorderColor = Color(0xFF2A2D3A)
val MutedColor = Color(0xFF64748B)
val AccentColor = Color(0xFF6366F1)
val AccentHover = Color(0xFF4F46E5)
val HighColor = Color(0xFFEF4444)
val MedColor = Color(0xFFF59E0B)
val LowColor = Color(0xFF22C55E)
val TextColor = Color(0xFFE2E8F0)

private val DarkColorScheme = darkColorScheme(
    primary = AccentColor,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3730A3),
    background = BgColor,
    surface = SurfaceColor,
    surfaceVariant = SurfaceColor,
    onBackground = TextColor,
    onSurface = TextColor,
    onSurfaceVariant = MutedColor,
    outline = BorderColor,
    error = HighColor,
)

@Composable
fun TodoAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}
