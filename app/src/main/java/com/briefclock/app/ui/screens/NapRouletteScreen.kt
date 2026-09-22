package com.briefclock.app.ui.screens

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.briefclock.app.R
import com.briefclock.app.audio.SoundEffects
import com.briefclock.app.data.BriefClockDatabase
import com.briefclock.app.model.NapRecord
import com.briefclock.app.ui.components.RevolverCanvas
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class RouletteState {
    IDLE,
    SLEEPING,
    RESULT
}

@Composable
fun NapRouletteScreen(
    context: Context = LocalContext.current,
    session: NapRouletteSession,
    database: BriefClockDatabase,
    onStatsUpdated: () -> Unit = {}
) {
    // Revolver Animation States
    val cylinderAngleAnim = remember { Animatable(0f) }
    val recoilAnim = remember { Animatable(0f) }
    var hammerCocked by remember { mutableStateOf(session.gameState == RouletteState.SLEEPING) }
    var triggerPulled by remember { mutableStateOf(false) }
    var muzzleFlash by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Timer while sleeping
    LaunchedEffect(session.gameState, session.startTimeMs) {
        if (session.gameState == RouletteState.SLEEPING) {
            while (session.gameState == RouletteState.SLEEPING) {
                delay(500)
                session.elapsedSec = ((System.currentTimeMillis() - session.startTimeMs) / 1000).toInt()
            }
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Title & Subtitle
        Text(
            text = stringResource(R.string.roulette_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = stringResource(R.string.roulette_rule_tip),
            fontSize = 12.sp,
            color = Color(0xFFFFB74D),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2.5D Revolver Canvas Component
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16191D)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                RevolverCanvas(
                    cylinderAngle = cylinderAngleAnim.value,
                    hammerCocked = hammerCocked,
                    triggerPulled = triggerPulled,
                    recoilAmount = recoilAnim.value,
                    muzzleFlash = muzzleFlash
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic State UI
        when (session.gameState) {
            RouletteState.IDLE -> {
                // Time Duration Selector Chips
                Text(
                    text = stringResource(R.string.roulette_target_time),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.LightGray
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val presets = listOf(
                        10 to stringResource(R.string.roulette_test_mode), // 10s for fast test
                        15 * 60 to "15m",
                        20 * 60 to "20m",
                        30 * 60 to "30m",
                        45 * 60 to "45m"
                    )

                    presets.forEach { (sec, label) ->
                        FilterChip(
                            selected = session.selectedChipSec == sec,
                            onClick = {
                                session.selectedChipSec = sec
                                session.targetDurationSec = sec
                            },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE53935),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Start Gamble Button
                Button(
                    onClick = {
                        coroutineScope.launch {
                            // Sound: Cylinder spin & Hammer cock
                            SoundEffects.playCylinderSpin(context)
                            cylinderAngleAnim.animateTo(
                                targetValue = cylinderAngleAnim.value + 720f,
                                animationSpec = tween(400)
                            )
                            delay(100)
                            SoundEffects.playHammerCock(context)
                            hammerCocked = true
                            triggerPulled = false
                            muzzleFlash = false

                            // Begin Nap
                            session.startTimeMs = System.currentTimeMillis()
                            session.elapsedSec = 0
                            session.gameState = RouletteState.SLEEPING
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.roulette_start),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            RouletteState.SLEEPING -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E232A)),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.roulette_sleeping),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB74D)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Elapsed Time display
                        Text(
                            text = formatDuration(session.elapsedSec),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        val remaining = (session.targetDurationSec - session.elapsedSec).coerceAtLeast(0)
                        Text(
                            text = if (remaining > 0) {
                                "${stringResource(R.string.roulette_remaining_time)}: ${formatDuration(remaining)}"
                            } else {
                                stringResource(R.string.roulette_overtime)
                            },
                            fontSize = 14.sp,
                            color = if (remaining > 0) Color.LightGray else Color(0xFF00E676),
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress Bar
                        val progress = (session.elapsedSec.toFloat() / session.targetDurationSec).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = if (progress >= 1f) Color(0xFF00E676) else Color(0xFFE53935),
                            trackColor = Color(0xFF374151),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Wake Up / End Nap Button
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val actual = ((System.currentTimeMillis() - session.startTimeMs) / 1000).toInt()
                            session.lastActualDurationSec = actual
                            val isSuccess = actual >= session.targetDurationSec
                            session.lastResultSuccess = isSuccess

                            // Pull trigger
                            triggerPulled = true
                            hammerCocked = false

                            if (isSuccess) {
                                // CLICK! Empty chamber -> Survived
                                SoundEffects.playTriggerClick(context)
                                cylinderAngleAnim.animateTo(cylinderAngleAnim.value + 60f, tween(150))
                            } else {
                                // BANG! Gunfire -> Defeated
                                SoundEffects.playGunshot(context)
                                muzzleFlash = true
                                recoilAnim.animateTo(1f, tween(50))
                                recoilAnim.animateTo(0f, tween(300))
                                delay(120)
                                muzzleFlash = false
                            }

                            // Save to database
                            database.insertNapRecord(
                                NapRecord(
                                    targetDurationSec = session.targetDurationSec,
                                    actualDurationSec = actual,
                                    isSuccess = isSuccess,
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                            onStatsUpdated()

                            session.gameState = RouletteState.RESULT
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (session.elapsedSec >= session.targetDurationSec) Color(0xFF00E676) else Color(0xFFE53935)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = stringResource(R.string.roulette_wake_up),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            RouletteState.RESULT -> {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (session.lastResultSuccess) Color(0xFF1B382B) else Color(0xFF3E1E1E)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (session.lastResultSuccess) {
                                stringResource(R.string.roulette_result_win_title)
                            } else {
                                stringResource(R.string.roulette_result_fail_title)
                            },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (session.lastResultSuccess) Color(0xFF00E676) else Color(0xFFFF5252)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (session.lastResultSuccess) {
                                context.getString(
                                    R.string.roulette_result_win_desc,
                                    formatDuration(session.targetDurationSec)
                                )
                            } else {
                                context.getString(
                                    R.string.roulette_result_fail_desc,
                                    formatDuration(session.lastActualDurationSec),
                                    formatDuration(session.targetDurationSec)
                                )
                            },
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                triggerPulled = false
                                hammerCocked = false
                                muzzleFlash = false
                                session.gameState = RouletteState.IDLE
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (session.lastResultSuccess) Color(0xFF00E676) else Color(0xFFE53935)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.roulette_play_again),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
