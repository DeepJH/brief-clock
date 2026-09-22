package com.briefclock.app.model

data class AlarmItem(
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "",
    val isEnabled: Boolean = true,
    val daysOfWeek: Int = 0, // Bitmask: Monday=1, Tue=2, Wed=4, Thu=8, Fri=16, Sat=32, Sun=64. 0 = Once
    val audioPath: String? = null,
    val isSystemAlarm: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val formattedTime: String
        get() = String.format("%02d:%02d", hour, minute)

    val isVoiceAlarm: Boolean
        get() = !audioPath.isNullOrEmpty()

    val isRepeat: Boolean
        get() = daysOfWeek > 0

    fun isDaySelected(dayIndex: Int): Boolean {
        // dayIndex 0..6 (Mon..Sun)
        return (daysOfWeek and (1 shl dayIndex)) != 0
    }
}
