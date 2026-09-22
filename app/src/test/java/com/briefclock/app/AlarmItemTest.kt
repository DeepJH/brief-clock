package com.briefclock.app

import com.briefclock.app.alarm.AlarmScheduler
import com.briefclock.app.model.AlarmItem
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class AlarmItemTest {

    @Test
    fun testFormattedTime() {
        val alarm1 = AlarmItem(hour = 7, minute = 5)
        assertEquals("07:05", alarm1.formattedTime)

        val alarm2 = AlarmItem(hour = 23, minute = 59)
        assertEquals("23:59", alarm2.formattedTime)

        val alarm3 = AlarmItem(hour = 0, minute = 0)
        assertEquals("00:00", alarm3.formattedTime)
    }

    @Test
    fun testDaySelectionBitmask() {
        // Mon=1, Tue=2, Wed=4, Thu=8, Fri=16, Sat=32, Sun=64
        // Weekdays: 1 + 2 + 4 + 8 + 16 = 31
        val weekdays = 1 or 2 or 4 or 8 or 16
        val alarm = AlarmItem(hour = 8, minute = 0, daysOfWeek = weekdays)

        assertTrue(alarm.isRepeat)
        assertTrue(alarm.isDaySelected(0)) // Mon
        assertTrue(alarm.isDaySelected(1)) // Tue
        assertTrue(alarm.isDaySelected(2)) // Wed
        assertTrue(alarm.isDaySelected(3)) // Thu
        assertTrue(alarm.isDaySelected(4)) // Fri
        assertFalse(alarm.isDaySelected(5)) // Sat
        assertFalse(alarm.isDaySelected(6)) // Sun
    }

    @Test
    fun testVoiceAlarmFlag() {
        val regularAlarm = AlarmItem(hour = 8, minute = 0, audioPath = null)
        assertFalse(regularAlarm.isVoiceAlarm)

        val voiceAlarm = AlarmItem(hour = 8, minute = 0, audioPath = "/path/to/voice.m4a")
        assertTrue(voiceAlarm.isVoiceAlarm)
    }

    @Test
    fun testCalculateTriggerTimeOnceInFuture() {
        val now = Calendar.getInstance()
        // 2 hours in the future
        val targetHour = (now.get(Calendar.HOUR_OF_DAY) + 2) % 24
        val targetMinute = now.get(Calendar.MINUTE)

        val alarm = AlarmItem(hour = targetHour, minute = targetMinute, daysOfWeek = 0)
        val triggerTime = AlarmScheduler.calculateTriggerTime(alarm)

        assertTrue(triggerTime > now.timeInMillis)
        // Should be within next 24 hours
        assertTrue(triggerTime - now.timeInMillis <= 24 * 3600 * 1000L + 60000L)
    }
}
