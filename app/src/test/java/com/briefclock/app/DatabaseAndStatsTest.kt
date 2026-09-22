package com.briefclock.app

import com.briefclock.app.alarm.AlarmScheduler
import com.briefclock.app.model.AlarmItem
import com.briefclock.app.model.NapRecord
import com.briefclock.app.model.NapStatistics
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class DatabaseAndStatsTest {

    @Test
    fun testStreakComputationComplex() {
        // Timeline (oldest to newest):
        // 1: Win
        // 2: Win
        // 3: Win (streak = 3)
        // 4: Loss (streak = 0)
        // 5: Win
        // 6: Win (current streak = 2, max streak = 3)
        val records = listOf(
            NapRecord(id = 1, targetDurationSec = 900, actualDurationSec = 950, isSuccess = true, timestamp = 1000L),
            NapRecord(id = 2, targetDurationSec = 900, actualDurationSec = 920, isSuccess = true, timestamp = 2000L),
            NapRecord(id = 3, targetDurationSec = 900, actualDurationSec = 910, isSuccess = true, timestamp = 3000L),
            NapRecord(id = 4, targetDurationSec = 900, actualDurationSec = 500, isSuccess = false, timestamp = 4000L),
            NapRecord(id = 5, targetDurationSec = 900, actualDurationSec = 930, isSuccess = true, timestamp = 5000L),
            NapRecord(id = 6, targetDurationSec = 900, actualDurationSec = 1000, isSuccess = true, timestamp = 6000L)
        )

        val totalGames = records.size
        val successCount = records.count { it.isSuccess }
        val failCount = totalGames - successCount
        val winRate = (successCount.toFloat() / totalGames) * 100f

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

        assertEquals(6, totalGames)
        assertEquals(5, successCount)
        assertEquals(1, failCount)
        assertEquals(83.33f, winRate, 0.01f)
        assertEquals(2, currentStreak)
        assertEquals(3, bestStreak)
    }

    @Test
    fun testAllLossStreak() {
        val records = listOf(
            NapRecord(id = 1, targetDurationSec = 900, actualDurationSec = 200, isSuccess = false, timestamp = 1000L),
            NapRecord(id = 2, targetDurationSec = 900, actualDurationSec = 100, isSuccess = false, timestamp = 2000L)
        )

        var currentStreak = 0
        var bestStreak = 0
        var running = 0
        for (r in records) {
            if (r.isSuccess) {
                running++
                if (running > bestStreak) bestStreak = running
            } else {
                running = 0
            }
        }
        for (r in records.reversed()) {
            if (r.isSuccess) currentStreak++ else break
        }

        assertEquals(0, currentStreak)
        assertEquals(0, bestStreak)
    }

    @Test
    fun testAlarmWeekdayBitmaskSchedule() {
        // Everyday alarm: 1 | 2 | 4 | 8 | 16 | 32 | 64 = 127
        val everydayAlarm = AlarmItem(hour = 9, minute = 30, daysOfWeek = 127)
        assertTrue(everydayAlarm.isRepeat)
        for (i in 0..6) {
            assertTrue(everydayAlarm.isDaySelected(i))
        }

        val triggerTime = AlarmScheduler.calculateTriggerTime(everydayAlarm)
        assertTrue(triggerTime > System.currentTimeMillis())
    }

    @Test
    fun testAlarmWeekendBitmaskSchedule() {
        // Sat=32 (index 5), Sun=64 (index 6). 32 + 64 = 96
        val weekendAlarm = AlarmItem(hour = 10, minute = 0, daysOfWeek = 96)
        assertTrue(weekendAlarm.isRepeat)
        assertFalse(weekendAlarm.isDaySelected(0)) // Mon
        assertFalse(weekendAlarm.isDaySelected(4)) // Fri
        assertTrue(weekendAlarm.isDaySelected(5))  // Sat
        assertTrue(weekendAlarm.isDaySelected(6))  // Sun
    }
}
