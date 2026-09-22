package com.briefclock.app.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class NapRouletteSession {
    var gameState by mutableStateOf(RouletteState.IDLE)
    var targetDurationSec by mutableIntStateOf(20 * 60)
    var selectedChipSec by mutableIntStateOf(20 * 60)
    var startTimeMs by mutableLongStateOf(0L)
    var elapsedSec by mutableIntStateOf(0)
    var lastResultSuccess by mutableStateOf(false)
    var lastActualDurationSec by mutableIntStateOf(0)
}
