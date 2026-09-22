package com.briefclock.app.model

data class NapRecord(
    val id: Long = 0,
    val targetDurationSec: Int,
    val actualDurationSec: Int,
    val isSuccess: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
