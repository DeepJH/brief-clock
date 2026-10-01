package com.briefclock.app.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.briefclock.app.R
import com.briefclock.app.audio.SoundEffects
import com.briefclock.app.data.BriefClockDatabase
import com.briefclock.app.model.NapRecord
import com.briefclock.app.ui.components.RevolverCanvas
import com.briefclock.app.ui.components.VerticalDurationWheelPicker
import com.briefclock.app.ui.components.generateNapOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

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
    onStatsUpdated: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    // Revolver Animation States
    val cylinderAngleAnim = remember { Animatable(0f) }
    val recoilAnim = remember { Animatable(0f) }
    var hammerCocked by remember { mutableStateOf(session.gameState == RouletteState.SLEEPING) }
    var triggerPulled by remember { mutableStateOf(false) }
    var muzzleFlash by remember { mutableStateOf(false) }

    var showRulesDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // 1-Second Periodic Red Flash for Wake-up Button during SLEEPING state
    var isRedFlash by remember { mutableStateOf(false) }
    LaunchedEffect(session.gameState) {
        if (session.gameState == RouletteState.SLEEPING) {
            while (session.gameState == RouletteState.SLEEPING) {
                delay(1000)
                isRedFlash = true
                delay(180)
                isRedFlash = false
            }
        } else {
            isRedFlash = false
        }
    }

    val wakeUpButtonColor by animateColorAsState(
        targetValue = if (isRedFlash) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 140),
        label = "redFlash"
    )

    // Easter Egg Tap Tracking: 10 taps in 10s spawns 💤
    val revolverTaps = remember { mutableStateListOf<Long>() }
    var isZzzVisible by remember { mutableStateOf(false) }
    var hasMissedEggInSession by remember { mutableStateOf(false) }
    var zzzXFraction by remember { mutableFloatStateOf(0.5f) }
    var zzzYFraction by remember { mutableFloatStateOf(0.4f) }
    var zzzBounds by remember { mutableStateOf<Rect?>(null) }
    var isEasterEggActive by remember { mutableStateOf(false) }

    // Silently track elapsed sleep time in background
    LaunchedEffect(session.gameState, session.startTimeMs) {
        if (session.gameState == RouletteState.SLEEPING) {
            while (session.gameState == RouletteState.SLEEPING) {
                delay(500)
                session.elapsedSec = ((System.currentTimeMillis() - session.startTimeMs) / 1000).toInt()
            }
        }
    }

    val isChinese = Locale.getDefault().language == "zh"
    val napOptions = remember(isChinese) { generateNapOptions(isChinese) }
    val scrollState = rememberScrollState()

    // Split-Screen curtain transition state for Easter Egg
    var splitCurtainOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main Screen Content (with split exit animation when Easter egg triggers)
        AnimatedVisibility(
            visible = !splitCurtainOpen,
            enter = fadeIn(tween(300)) + slideInVertically(tween(400)) { it / 3 },
            exit = fadeOut(tween(300)) + slideOutVertically(tween(400)) { -it / 2 }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isZzzVisible, zzzBounds) {
                        if (isZzzVisible) {
                            awaitPointerEventScope {
                                while (isZzzVisible) {
                                    val event = awaitPointerEvent(PointerEventPass.Initial)
                                    val firstPressed = event.changes.firstOrNull { it.pressed }
                                    if (firstPressed != null) {
                                        val pos = firstPressed.position
                                        val bounds = zzzBounds
                                        if (bounds != null && bounds.contains(pos)) {
                                            // Clicked on 💤 -> Enter Easter egg!
                                            firstPressed.consume()
                                            isZzzVisible = false
                                            SoundEffects.playCylinderSpin(context)
                                            splitCurtainOpen = true
                                            coroutineScope.launch {
                                                delay(380)
                                                isEasterEggActive = true
                                            }
                                        } else {
                                            // Clicked outside 💤 -> Hide 💤, mark missed for session!
                                            isZzzVisible = false
                                            hasMissedEggInSession = true
                                        }
                                    }
                                }
                            }
                        }
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Row: Title + Rules Button + Settings Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.roulette_title),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = stringResource(R.string.roulette_subtitle),
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { showRulesDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = stringResource(R.string.roulette_rules_title),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = onOpenSettings) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = stringResource(R.string.settings_title),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 2D Vector Revolver Canvas with Interactive Tap Zones:
                    // Left half (barrel): Gunshot + Recoil + Muzzle Flash
                    // Right half (cylinder): Cylinder Spin Sound + 360° Animation
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    val now = System.currentTimeMillis()

                                    // Easter egg tap counter (10 taps in 10s)
                                    revolverTaps.removeAll { now - it > 10000L }
                                    revolverTaps.add(now)
                                    if (revolverTaps.size >= 10 && !isZzzVisible && !isEasterEggActive && !hasMissedEggInSession) {
                                        isZzzVisible = true
                                        zzzXFraction = Random.nextFloat() * 0.65f + 0.15f
                                        zzzYFraction = Random.nextFloat() * 0.45f + 0.25f
                                        revolverTaps.clear()
                                    }

                                    val isLeftHalf = offset.x < size.width / 2f
                                    if (isLeftHalf) {
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

                            // 1-Minute Increment Wheel Picker (10s test + 1m..60m)
                            VerticalDurationWheelPicker(
                                options = napOptions,
                                selectedSeconds = session.targetDurationSec,
                                onDurationSelected = { sec ->
                                    session.targetDurationSec = sec
                                    session.selectedChipSec = sec
                                },
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Start Gamble Button ("开赌！")
                            Button(
                                onClick = {
                                    coroutineScope.launch {
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
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        RouletteState.SLEEPING -> {
                            // When gambling: NO time displayed! Vertically centered in lower area.
                            // Button flashes red every 1 second ("每过一秒就变红一下").
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            triggerPulled = true
                                            delay(40)

                                            val isSuccess = session.elapsedSec >= session.targetDurationSec
                                            session.lastResultSuccess = isSuccess

                                            if (isSuccess) {
                                                SoundEffects.playTriggerClick(context)
                                                hammerCocked = false
                                            } else {
                                                SoundEffects.playGunshot(context)
                                                muzzleFlash = true
                                                hammerCocked = false

                                                recoilAnim.animateTo(1f, tween(60))
                                                recoilAnim.animateTo(0f, tween(250))
                                                muzzleFlash = false
                                            }

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
                                    colors = ButtonDefaults.buttonColors(containerColor = wakeUpButtonColor),
                                    shape = RoundedCornerShape(percent = 50),
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .height(64.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = stringResource(R.string.roulette_wake_up), // "醒来"
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
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

                                    // Stats Summary Row
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

                // Easter Egg Floating 💤 Trigger (Track bounds so clicks outside dismiss non-blockingly)
                if (isZzzVisible) {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val posX = maxWidth * zzzXFraction
                        val posY = maxHeight * zzzYFraction

                        val infiniteFloat = rememberInfiniteTransition(label = "zzzFloat")
                        val zzzScale by infiniteFloat.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.35f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "scale"
                        )

                        Box(
                            modifier = Modifier
                                .offset(x = posX, y = posY)
                                .onGloballyPositioned { coordinates ->
                                    zzzBounds = coordinates.boundsInParent()
                                }
                                .scale(zzzScale)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f))
                                .padding(12.dp)
                        ) {
                            Text(text = "💤", fontSize = 32.sp)
                        }
                    }
                }
            }
        }

        // Easter Egg Mini-Game Active Screen
        if (isEasterEggActive) {
            EasterEggGameScreen(
                onDismissGame = {
                    isEasterEggActive = false
                    splitCurtainOpen = false
                }
            )
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
