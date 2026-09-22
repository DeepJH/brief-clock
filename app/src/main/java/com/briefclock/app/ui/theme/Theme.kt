package com.briefclock.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE53935),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF8C1D18),
    onPrimaryContainer = Color(0xFFFFCDD2),
    secondary = Color(0xFF00E676),
    onSecondary = Color.Black,
    background = Color(0xFF121417),
    onBackground = Color(0xFFECEFF1),
    surface = Color(0xFF1A1E24),
    onSurface = Color(0xFFECEFF1),
    surfaceVariant = Color(0xFF262C34),
    onSurfaceVariant = Color(0xFFB0BEC5)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFD32F2F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEBEE),
    onPrimaryContainer = Color(0xFFB71C1C),
    secondary = Color(0xFF00C853),
    onSecondary = Color.White,
    background = Color(0xFFF7F8FA),
    onBackground = Color(0xFF1A1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFECEFF1),
    onSurfaceVariant = Color(0xFF455A64)
)

@Composable
fun BriefClockTheme(
    darkTheme: Boolean = true, // Default to sleek tactical dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
