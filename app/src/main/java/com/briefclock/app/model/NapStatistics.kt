package com.briefclock.app.model

data class DailyNapDuration(
    val dateLabel: String, // e.g. "09/22" or "Mon"
    val totalMinutes: Int,
    val successCount: Int,
    val failCount: Int
)

data class NapStatistics(
    val totalGames: Int = 0,
    val successCount: Int = 0,
    val failCount: Int = 0,
    val winRatePercent: Float = 0f,
    val totalDurationMinutes: Int = 0,
    val avgDurationMinutes: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val recentDays: List<DailyNapDuration> = emptyList(),
    val recentRecords: List<NapRecord> = emptyList()
)
