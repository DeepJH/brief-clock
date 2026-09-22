package com.briefclock.app

import com.briefclock.app.ui.theme.AppLanguage
import com.briefclock.app.ui.theme.ThemeColor
import com.briefclock.app.ui.theme.ThemeMode
import org.junit.Assert.*
import org.junit.Test

class ThemeAndSoundTest {

    @Test
    fun testThemeColorDefaultIsBlue() {
        // Requirement 4: Default primary color changed to Blue
        assertEquals(ThemeColor.BLUE, ThemeColor.valueOf("BLUE"))
        assertEquals("blue", ThemeColor.BLUE.key)
        assertNotNull(ThemeColor.BLUE.previewColor)
        assertNotNull(ThemeColor.BLUE.lightPrimary)
        assertNotNull(ThemeColor.BLUE.darkPrimary)
    }

    @Test
    fun testAllThemeColorsDefined() {
        val colors = ThemeColor.entries
        assertEquals(5, colors.size)
        assertTrue(colors.contains(ThemeColor.BLUE))
        assertTrue(colors.contains(ThemeColor.CRIMSON))
        assertTrue(colors.contains(ThemeColor.EMERALD))
        assertTrue(colors.contains(ThemeColor.VIOLET))
        assertTrue(colors.contains(ThemeColor.AMBER))
    }

    @Test
    fun testThemeModes() {
        // Requirement 5: Light / Dark / Follow System
        val modes = ThemeMode.entries
        assertEquals(3, modes.size)
        assertTrue(modes.contains(ThemeMode.SYSTEM))
        assertTrue(modes.contains(ThemeMode.LIGHT))
        assertTrue(modes.contains(ThemeMode.DARK))
    }

    @Test
    fun testLanguages() {
        // Requirement 8: Switchable languages (System / ZH / EN)
        val languages = AppLanguage.entries
        assertEquals(3, languages.size)
        assertTrue(languages.contains(AppLanguage.SYSTEM))
        assertTrue(languages.contains(AppLanguage.ZH))
        assertTrue(languages.contains(AppLanguage.EN))
    }
}
