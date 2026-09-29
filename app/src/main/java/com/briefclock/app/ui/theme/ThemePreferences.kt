package com.briefclock.app.ui.theme

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import androidx.compose.ui.graphics.Color
import java.util.Locale

enum class ThemeMode(val key: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark")
}

enum class ThemeColor(
    val key: String,
    val displayNameRes: String,
    val previewColor: Color,
    // Light palette
    val lightPrimary: Color,
    val lightOnPrimary: Color,
    val lightPrimaryContainer: Color,
    val lightOnPrimaryContainer: Color,
    val lightSecondary: Color,
    // Dark palette
    val darkPrimary: Color,
    val darkOnPrimary: Color,
    val darkPrimaryContainer: Color,
    val darkOnPrimaryContainer: Color,
    val darkSecondary: Color
) {
    BLUE(
        key = "blue",
        displayNameRes = "Blue",
        previewColor = Color(0xFF2563EB),
        // Light
        lightPrimary = Color(0xFF1D4ED8),
        lightOnPrimary = Color.White,
        lightPrimaryContainer = Color(0xFFDBEAFE),
        lightOnPrimaryContainer = Color(0xFF1E3A8A),
        lightSecondary = Color(0xFF0284C7),
        // Dark
        darkPrimary = Color(0xFF60A5FA),
        darkOnPrimary = Color(0xFF0F172A),
        darkPrimaryContainer = Color(0xFF1E40AF),
        darkOnPrimaryContainer = Color(0xFFDBEAFE),
        darkSecondary = Color(0xFF38BDF8)
    ),
    CRIMSON(
        key = "crimson",
        displayNameRes = "Crimson",
        previewColor = Color(0xFFDC2626),
        // Light
        lightPrimary = Color(0xFFDC2626),
        lightOnPrimary = Color.White,
        lightPrimaryContainer = Color(0xFFFEE2E2),
        lightOnPrimaryContainer = Color(0xFF991B1B),
        lightSecondary = Color(0xFFEA580C),
        // Dark
        darkPrimary = Color(0xFFF87171),
        darkOnPrimary = Color(0xFF450A0A),
        darkPrimaryContainer = Color(0xFF991B1B),
        darkOnPrimaryContainer = Color(0xFFFEE2E2),
        darkSecondary = Color(0xFFFB923C)
    ),
    EMERALD(
        key = "emerald",
        displayNameRes = "Emerald",
        previewColor = Color(0xFF059669),
        // Light
        lightPrimary = Color(0xFF059669),
        lightOnPrimary = Color.White,
        lightPrimaryContainer = Color(0xFFD1FAE5),
        lightOnPrimaryContainer = Color(0xFF065F46),
        lightSecondary = Color(0xFF0D9488),
        // Dark
        darkPrimary = Color(0xFF34D399),
        darkOnPrimary = Color(0xFF022C22),
        darkPrimaryContainer = Color(0xFF065F46),
        darkOnPrimaryContainer = Color(0xFFD1FAE5),
        darkSecondary = Color(0xFF2DD4BF)
    ),
    VIOLET(
        key = "violet",
        displayNameRes = "Violet",
        previewColor = Color(0xFF7C3AED),
        // Light
        lightPrimary = Color(0xFF7C3AED),
        lightOnPrimary = Color.White,
        lightPrimaryContainer = Color(0xFFEDE9FE),
        lightOnPrimaryContainer = Color(0xFF4C1D95),
        lightSecondary = Color(0xFF9333EA),
        // Dark
        darkPrimary = Color(0xFFA78BFA),
        darkOnPrimary = Color(0xFF2E1065),
        darkPrimaryContainer = Color(0xFF5B21B6),
        darkOnPrimaryContainer = Color(0xFFEDE9FE),
        darkSecondary = Color(0xFFC084FC)
    ),
    AMBER(
        key = "amber",
        displayNameRes = "Amber",
        previewColor = Color(0xFFD97706),
        // Light
        lightPrimary = Color(0xFFD97706),
        lightOnPrimary = Color.White,
        lightPrimaryContainer = Color(0xFFFEF3C7),
        lightOnPrimaryContainer = Color(0xFF78350F),
        lightSecondary = Color(0xFFCA8A04),
        // Dark
        darkPrimary = Color(0xFFFBBF24),
        darkOnPrimary = Color(0xFF451A03),
        darkPrimaryContainer = Color(0xFF92400E),
        darkOnPrimaryContainer = Color(0xFFFEF3C7),
        darkSecondary = Color(0xFFFACC15)
    )
}

enum class AppLanguage(val code: String) {
    SYSTEM("system"),
    ZH("zh"),
    EN("en")
}

class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("brief_clock_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val KEY_THEME_COLOR = "pref_theme_color"
        private const val KEY_LANGUAGE = "pref_language"

        fun getLocaleForLanguage(lang: AppLanguage): Locale {
            return when (lang) {
                AppLanguage.ZH -> Locale.SIMPLIFIED_CHINESE
                AppLanguage.EN -> Locale.ENGLISH
                AppLanguage.SYSTEM -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Resources.getSystem().configuration.locales[0]
                    } else {
                        @Suppress("DEPRECATION")
                        Resources.getSystem().configuration.locale
                    }
                }
            }
        }

        fun applyLanguage(context: Context, lang: AppLanguage): Locale {
            val locale = getLocaleForLanguage(lang)
            Locale.setDefault(locale)

            val res = context.resources
            val config = Configuration(res.configuration)
            config.setLocale(locale)
            @Suppress("DEPRECATION")
            res.updateConfiguration(config, res.displayMetrics)

            val appRes = context.applicationContext.resources
            val appConfig = Configuration(appRes.configuration)
            appConfig.setLocale(locale)
            @Suppress("DEPRECATION")
            appRes.updateConfiguration(appConfig, appRes.displayMetrics)

            return locale
        }
    }

    var themeMode: ThemeMode
        get() {
            val v = prefs.getString(KEY_THEME_MODE, ThemeMode.DARK.key)
            return ThemeMode.entries.find { it.key == v } ?: ThemeMode.DARK
        }
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value.key).apply()

    var themeColor: ThemeColor
        get() {
            val v = prefs.getString(KEY_THEME_COLOR, ThemeColor.BLUE.key)
            return ThemeColor.entries.find { it.key == v } ?: ThemeColor.BLUE
        }
        set(value) = prefs.edit().putString(KEY_THEME_COLOR, value.key).apply()

    var language: AppLanguage
        get() {
            val v = prefs.getString(KEY_LANGUAGE, AppLanguage.SYSTEM.code)
            return AppLanguage.entries.find { it.code == v } ?: AppLanguage.SYSTEM
        }
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value.code).apply()
}
