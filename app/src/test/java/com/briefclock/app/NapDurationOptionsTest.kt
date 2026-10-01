package com.briefclock.app

import com.briefclock.app.ui.components.generateNapOptions
import org.junit.Assert.*
import org.junit.Test

class NapDurationOptionsTest {

    @Test
    fun testOneMinuteIncrements() {
        val options = generateNapOptions(isChinese = true)
        assertEquals(61, options.size)
        // First option is 10s test mode
        assertEquals(10, options[0].seconds)
        assertTrue(options[0].label.contains("10"))

        // Subsequent options are 1m..60m
        assertEquals(60, options[1].seconds)
        assertEquals(120, options[2].seconds)
        assertEquals(180, options[3].seconds)
        assertEquals(60 * 60, options.last().seconds)
    }

    @Test
    fun testEnglishOptions() {
        val options = generateNapOptions(isChinese = false)
        assertEquals(61, options.size)
        assertEquals(10, options[0].seconds)
        assertTrue(options[0].label.contains("Test"))
        assertEquals("1 min", options[1].label)
        assertEquals("60 min", options.last().label)
    }
}
