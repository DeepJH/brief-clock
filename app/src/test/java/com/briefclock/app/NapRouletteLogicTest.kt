package com.briefclock.app

import com.briefclock.app.model.DailyNapDuration
import com.briefclock.app.model.NapRecord
import com.briefclock.app.model.NapStatistics
import org.junit.Assert.*
import org.junit.Test

class NapRouletteLogicTest {

    @Test
    fun testVictoryCondition() {
        val targetSec = 15 * 60
        val actualSec = 15 * 60 + 10 // Slept 10s extra
        val isSuccess = actualSec >= targetSec
        assertTrue(isSuccess)
    }

    @Test
    fun testDefeatCondition() {
        val targetSec = 20 * 60
        val actualSec = 10 * 60 // Woke up 10 min too early
        val isSuccess = actualSec >= targetSec
        assertFalse(isSuccess)
    }

    @Test
    fun testStatisticsCalculation() {
        val records = listOf(
            NapRecord(id = 1, targetDurationSec = 600, actualDurationSec = 600, isSuccess = true, timestamp = 1000L),
            NapRecord(id = 2, targetDurationSec = 600, actualDurationSec = 300, isSuccess = false, timestamp = 2000L),
            NapRecord(id = 3, targetDurationSec = 600, actualDurationSec = 650, isSuccess = true, timestamp = 3000L),
            NapRecord(id = 4, targetDurationSec = 600, actualDurationSec = 600, isSuccess = true, timestamp = 4000L)
        )

        val total = records.size
        val success = records.count { it.isSuccess }
        val fail = total - success
        val winRate = success.toFloat() / total * 100f
        val totalDurationSec = records.sumOf { it.actualDurationSec }

        assertEquals(4, total)
        assertEquals(3, success)
        assertEquals(1, fail)
        assertEquals(75.0f, winRate, 0.01f)
        assertEquals(2150, totalDurationSec)

        // Streak test: chronological order
        var currentStreak = 0
        var bestStreak = 0
        var runningStreak = 0
        for (rec in records) {
            if (rec.isSuccess) {
                runningStreak++
                if (runningStreak > bestStreak) bestStreak = runningStreak
            } else {
                runningStreak = 0
            }
        }
        for (rec in records.reversed()) {
            if (rec.isSuccess) currentStreak++ else break
        }

        assertEquals(2, currentStreak) // last two were success
        assertEquals(2, bestStreak)
    }

    @Test
    fun testEmptyStatistics() {
        val stats = NapStatistics()
        assertEquals(0, stats.totalGames)
        assertEquals(0, stats.successCount)
        assertEquals(0, stats.failCount)
        assertEquals(0f, stats.winRatePercent, 0.001f)
        assertEquals(0, stats.currentStreak)
        assertEquals(0, stats.bestStreak)
    }
}
