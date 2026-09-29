package com.briefclock.app.ui.screens

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.briefclock.app.R
import com.briefclock.app.audio.SoundEffects
import com.briefclock.app.data.BriefClockDatabase
import com.briefclock.app.model.NapRecord
import com.briefclock.app.ui.components.RevolverCanvas
import com.briefclock.app.ui.components.VerticalDurationWheelPicker
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

    var showRulesDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Timer while sleeping (tracked silently in background)
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
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Screen Header: Nap Roulette + Rules Info Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.roulette_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.roulette_subtitle),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = { showRulesDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = stringResource(R.string.roulette_rules_title),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 3D Unshaded Revolver Canvas with Interactive Tap Zones:
        // Left half (barrel): Fire Gunshot Sound + Recoil + Muzzle Flash
        // Right half (cylinder): Cylinder Spin Sound + Cylinder Rotation Animation
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val isLeftHalf = offset.x < size.width / 2f
                        if (isLeftHalf) {
                            // Tap left half: Gunshot sound + recoil + flash
                            coroutineScope.launch {
                                SoundEffects.playGunshot(context)
                                triggerPulled = true
                                muzzleFlash = true
                                recoilAnim.snapTo(0f)
                                recoilAnim.animateTo(1f, tween(50))
                                muzzleFlash = false
                                recoilAnim.animateTo(0f, tween(240))
                                triggerPulled = false
                            }
                        } else {
                            // Tap right half: Cylinder spin sound + rotation
                            coroutineScope.launch {
                                SoundEffects.playCylinderSpin(context)
                                cylinderAngleAnim.animateTo(
                                    targetValue = cylinderAngleAnim.value + 360f,
                                    animationSpec = tween(400)
                                )
                            }
                        }
                    }
                }
        ) {
            RevolverCanvas(
                cylinderAngle = cylinderAngleAnim.value,
                hammerCocked = hammerCocked,
                triggerPulled = triggerPulled,
                recoilAmount = recoilAnim.value,
                muzzleFlash = muzzleFlash
            )
        }

        // Tap hint guide
        Text(
            text = stringResource(R.string.roulette_interactive_hint),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Dynamic State UI
        when (session.gameState) {
            RouletteState.IDLE -> {
                Text(
                    text = stringResource(R.string.roulette_target_time),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Vertical Duration Drum/Wheel Picker
                VerticalDurationWheelPicker(
                    selectedSeconds = session.targetDurationSec,
                    onDurationSelected = { sec ->
                        session.targetDurationSec = sec
                        session.selectedChipSec = sec
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Start Gamble Button (Capsule shaped, primary theme color)
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(percent = 50),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(54.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.roulette_start),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            RouletteState.SLEEPING -> {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.roulette_sleeping),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // True Gamble: Time is strictly hidden while sleeping!
                        Text(
                            text = "? ? : ? ?",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = stringResource(R.string.roulette_sleeping_blind_hint),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        // Wake Up / Pull Trigger Button (Neutral theme color to avoid leaking whether target was met)
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    triggerPulled = true
                                    delay(40)

                                    val isSuccess = session.elapsedSec >= session.targetDurationSec
                                    session.lastResultSuccess = isSuccess

                                    if (isSuccess) {
                                        // Empty Chamber Click
                                        SoundEffects.playTriggerClick(context)
                                        hammerCocked = false
                                    } else {
                                        // BANG! Gunshot & Recoil
                                        SoundEffects.playGunshot(context)
                                        muzzleFlash = true
                                        hammerCocked = false

                                        // Recoil kickback animation
                                        recoilAnim.animateTo(1f, tween(60))
                                        recoilAnim.animateTo(0f, tween(250))
                                        muzzleFlash = false
                                    }

                                    // Record to Database
                                    val record = NapRecord(
                                        targetDurationSec = session.targetDurationSec,
                                        actualDurationSec = session.elapsedSec,
                                        isSuccess = isSuccess,
                                        timestamp = System.currentTimeMillis()
                                    )
                                    database.insertNapRecord(record)
                                    onStatsUpdated()

                                    session.gameState = RouletteState.RESULT
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(percent = 50),
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(52.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.roulette_wake_up),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            RouletteState.RESULT -> {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val survived = session.lastResultSuccess

                        Text(
                            text = if (survived) "💥 CLICK!" else "🔥 BANG!",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (survived) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (survived) {
                                stringResource(R.string.roulette_survived_desc)
                            } else {
                                stringResource(R.string.roulette_shot_desc)
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Summary Row (Reveals target vs actual time only after pulling trigger)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    stringResource(R.string.stats_target),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    formatDuration(session.targetDurationSec),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    stringResource(R.string.stats_actual),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    formatDuration(session.elapsedSec),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (survived) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Play Again Button
                        Button(
                            onClick = {
                                hammerCocked = false
                                triggerPulled = false
                                muzzleFlash = false
                                session.gameState = RouletteState.IDLE
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(percent = 50),
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(50.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.roulette_play_again),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }

    // Rules Info Popup Dialog
    if (showRulesDialog) {
        AlertDialog(
            onDismissRequest = { showRulesDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.roulette_rules_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.roulette_rules_content),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = { showRulesDialog = false }) {
                    Text(stringResource(R.string.settings_done), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
