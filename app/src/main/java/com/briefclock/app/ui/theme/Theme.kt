package com.briefclock.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun BriefClockTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    themeColor: ThemeColor = ThemeColor.BLUE,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = themeColor.darkPrimary,
            onPrimary = themeColor.darkOnPrimary,
            primaryContainer = themeColor.darkPrimaryContainer,
            onPrimaryContainer = themeColor.darkOnPrimaryContainer,
            secondary = themeColor.darkSecondary,
            onSecondary = Color.Black,
            secondaryContainer = themeColor.darkPrimaryContainer.copy(alpha = 0.4f),
            onSecondaryContainer = themeColor.darkOnPrimaryContainer,
            tertiary = Color(0xFF38BDF8),
            onTertiary = Color(0xFF0C4A6E),
            background = Color(0xFF0F1216),
            onBackground = Color(0xFFE2E8F0),
            surface = Color(0xFF161B22),
            onSurface = Color(0xFFF1F5F9),
            surfaceVariant = Color(0xFF21262D),
            onSurfaceVariant = Color(0xFF94A3B8),
            surfaceContainer = Color(0xFF1C2128),
            surfaceContainerHigh = Color(0xFF262C36),
            surfaceContainerLow = Color(0xFF12151B),
            outline = Color(0xFF30363D),
            outlineVariant = Color(0xFF21262D),
            error = Color(0xFFF87171),
            onError = Color(0xFF450A0A)
        )
    } else {
        lightColorScheme(
            primary = themeColor.lightPrimary,
            onPrimary = themeColor.lightOnPrimary,
            primaryContainer = themeColor.lightPrimaryContainer,
            onPrimaryContainer = themeColor.lightOnPrimaryContainer,
            secondary = themeColor.lightSecondary,
            onSecondary = Color.White,
            secondaryContainer = themeColor.lightPrimaryContainer.copy(alpha = 0.6f),
            onSecondaryContainer = themeColor.lightOnPrimaryContainer,
            tertiary = Color(0xFF0284C7),
            onTertiary = Color.White,
            background = Color(0xFFF8FAFC),
            onBackground = Color(0xFF0F172A),
            surface = Color.White,
            onSurface = Color(0xFF0F172A),
            surfaceVariant = Color(0xFFF1F5F9),
            onSurfaceVariant = Color(0xFF64748B),
            surfaceContainer = Color(0xFFFFFFFF),
            surfaceContainerHigh = Color(0xFFE2E8F0),
            surfaceContainerLow = Color(0xFFF8FAFC),
            outline = Color(0xFFCBD5E1),
            outlineVariant = Color(0xFFE2E8F0),
            error = Color(0xFFDC2626),
            onError = Color.White
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
