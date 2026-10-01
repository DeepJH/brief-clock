package com.briefclock.app.ui.screens

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.briefclock.app.R
import com.briefclock.app.audio.SoundEffects
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

class ActiveFlyingEmoji(
    val id: Long,
    val emoji: String,
    val angle: Float,
    val speed: Float, // dp per second
    val fadeDurationSec: Float, // 0.15 - 0.45s
    var currentDistDp: Float = 0f,
    var ageSec: Float = 0f,
    var isHit: Boolean = false
)

data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    val size: Float
)

@Composable
fun EasterEggGameScreen(
    onDismissGame: () -> Unit
) {
    val context = LocalContext.current
    var score by remember { mutableIntStateOf(0) }
    var timeLeftSec by remember { mutableFloatStateOf(10f) }
    var isGameOver by remember { mutableStateOf(false) }

    val prefs = remember { context.getSharedPreferences("easter_egg_prefs", Context.MODE_PRIVATE) }
    var bestScore by remember { mutableIntStateOf(prefs.getInt("key_best_egg_score", 0)) }

    val emojis = remember { mutableStateListOf<ActiveFlyingEmoji>() }
    var nextEmojiId by remember { mutableLongStateOf(0L) }
    val confettiList = remember { mutableStateListOf<ConfettiParticle>() }

    // Psychedelic warp wave animation phase
    var ringPhase by remember { mutableFloatStateOf(0f) }

    // 60 FPS Game Loop
    LaunchedEffect(Unit) {
        var lastTime = System.nanoTime()
        var spawnTimer = 0f

        while (timeLeftSec > 0f) {
            delay(16) // ~60 FPS
            val now = System.nanoTime()
            val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
            lastTime = now

            timeLeftSec = (timeLeftSec - dt).coerceAtLeast(0f)

            // Speed multiplier: starts at 1.0x, accelerates smoothly to 5.0x by the end!
            val progress = (10f - timeLeftSec) / 10f
            val currentSpeedMultiplier = 1f + progress * 4.0f // 1.0x -> 5.0x

            // Advance psychedelic ring expansion phase
            ringPhase += dt * 0.4f * currentSpeedMultiplier

            // Spawn new emojis continuously
            spawnTimer += dt
            val spawnInterval = (0.24f / (1f + progress * 0.8f)).coerceAtLeast(0.12f)
            if (spawnTimer >= spawnInterval) {
                spawnTimer = 0f
                val candidateEmojis = listOf("😴", "💤", "🛌")
                val angle = Random.nextFloat() * 2f * PI.toFloat()
                val speed = (Random.nextFloat() * 160f + 120f) * (1f + progress * 0.5f)
                val fadeDur = Random.nextFloat() * 0.3f + 0.15f // 0.15s - 0.45s

                emojis.add(
                    ActiveFlyingEmoji(
                        id = nextEmojiId++,
                        emoji = candidateEmojis.random(),
                        angle = angle,
                        speed = speed,
                        fadeDurationSec = fadeDur
                    )
                )
            }

            // Update flying emojis positions and alpha
            val iterator = emojis.iterator()
            while (iterator.hasNext()) {
                val e = iterator.next()
                if (e.isHit || e.currentDistDp > 500f) {
                    iterator.remove()
                } else {
                    e.ageSec += dt
                    e.currentDistDp += e.speed * dt
                }
            }
        }

        // 10s Elapsed -> Game Over!
        isGameOver = true
        SoundEffects.playGunshot(context)
        if (score > bestScore) {
            bestScore = score
            prefs.edit().putInt("key_best_egg_score", bestScore).apply()
        }

        // Spawn confetti fireworks
        val confettiColors = listOf(
            Color(0xFFFF007F), Color(0xFF00F0FF), Color(0xFF76FF03),
            Color(0xFFFFD600), Color(0xFFFF6D00), Color(0xFF7B1FA2)
        )
        for (i in 0..160) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = Random.nextFloat() * 14f + 4f
            confettiList.add(
                ConfettiParticle(
                    x = 0.5f,
                    y = 0.4f,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 5f,
                    color = confettiColors.random(),
                    size = Random.nextFloat() * 9f + 4f
                )
            )
        }

        // Confetti physics loop
        while (true) {
            delay(16)
            confettiList.forEach { p ->
                p.x += p.vx / 400f
                p.y += p.vy / 800f
                p.vy += 0.25f // gravity
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Pure white initial background
            .pointerInput(isGameOver) {
                if (!isGameOver) {
                    detectTapGestures { tapOffset ->
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()
                        val cx = w / 2f
                        val cy = h / 2f

                        // Check hit with active flying emojis
                        for (e in emojis) {
                            if (e.isHit) continue
                            val distPx = e.currentDistDp * density
                            val ex = cx + distPx * cos(e.angle)
                            val ey = cy + distPx * sin(e.angle)
                            val touchRadius = 52.dp.toPx()

                            if (hypot(tapOffset.x - ex, tapOffset.y - ey) <= touchRadius) {
                                e.isHit = true
                                score++
                                SoundEffects.playGunshot(context)
                                break
                            }
                        }
                    }
                }
            }
    ) {
        // 1. Organic Psychedelic Color Rings (Surging outward, very thick, soft/blurred, accelerates 5x)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val maxR = hypot(cx, cy) * 1.2f

            // High-saturation psychedelic colors (吃毒蘑菇般的迷幻感)
            val ringColors = listOf(
                Color(0xFFFF007F), // Neon Magenta
                Color(0xFF00F0FF), // Vivid Cyan
                Color(0xFF76FF03), // Acid Lime
                Color(0xFF7B1FA2), // Electric Violet
                Color(0xFFFF6D00), // Neon Orange
                Color(0xFFFFEA00)  // Bright Yellow
            )

            val ringCount = 8
            val numPoints = 40
            val wobblePhase = ringPhase * 2f

            for (r in 0 until ringCount) {
                val p = (ringPhase + r.toFloat() / ringCount) % 1f
                val radius = (p * p) * maxR // Perspective expansion
                val alpha = (sin(p * PI.toFloat())).coerceIn(0f, 1f) * 0.42f
                val strokeW = (p * 50f + 16f) * density // Very thick stroke

                val path = Path()
                for (i in 0 until numPoints) {
                    val theta = i * (2f * PI.toFloat() / numPoints)
                    // Organic irregular psychedelic wobble
                    val wobble = 1f + 0.12f * sin(3f * theta + wobblePhase + r) + 0.07f * cos(5f * theta - wobblePhase)
                    val rEff = radius * wobble
                    val px = cx + rEff * cos(theta)
                    val py = cy + rEff * sin(theta)
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()

                drawPath(
                    path = path,
                    color = ringColors[r % ringColors.size].copy(alpha = alpha),
                    style = Stroke(width = strokeW)
                )
            }
        }

        // 2. Flying Emojis (Smooth continuous 60fps flight, fades in from 0% to 100% within 0.15-0.45s)
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val cx = maxWidth / 2
            val cy = maxHeight / 2

            emojis.forEach { e ->
                if (!e.isHit) {
                    val ex = cx + (e.currentDistDp * cos(e.angle)).dp
                    val ey = cy + (e.currentDistDp * sin(e.angle)).dp

                    // Alpha smoothly transitions from 0f to 1f within fadeDurationSec
                    val alpha = (e.ageSec / e.fadeDurationSec).coerceIn(0f, 1f)
                    val scaleFactor = (0.7f + (e.currentDistDp / 180f)).coerceIn(0.7f, 2.6f)
                    val fontSize = (24 * scaleFactor).sp

                    Text(
                        text = e.emoji,
                        fontSize = fontSize,
                        modifier = Modifier
                            .offset(x = ex - (fontSize.value / 2).dp, y = ey - (fontSize.value / 2).dp)
                            .alpha(alpha)
                    )
                }
            }
        }

        // 3. Top HUD: Real-time Score & Countdown Timer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(percent = 50),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 6.dp
            ) {
                Text(
                    text = "🎯 击中: $score",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                )
            }

            val remainingInt = ceil(timeLeftSec).toInt()
            Surface(
                shape = RoundedCornerShape(percent = 50),
                color = if (remainingInt <= 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 6.dp
            ) {
                Text(
                    text = "⏳ ${remainingInt}s",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (remainingInt <= 3) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                )
            }
        }

        // 4. Results Dialog with Big Centered Button
        if (isGameOver) {
            AlertDialog(
                onDismissRequest = onDismissGame,
                shape = RoundedCornerShape(24.dp),
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "🎉 " + stringResource(R.string.easter_egg_result_title),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = String.format(stringResource(R.string.easter_egg_score), score),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = String.format(stringResource(R.string.easter_egg_best), bestScore),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    // Big, prominent, centered button ("太棒了")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = onDismissGame,
                            shape = RoundedCornerShape(percent = 50),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(56.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.easter_egg_confirm),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            )
        }

        // 5. Confetti Fireworks Layer IN FRONT OF the Dialog
        if (isGameOver && confettiList.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                confettiList.forEach { p ->
                    p.x += p.vx / 400f
                    p.y += p.vy / 800f
                    drawCircle(
                        color = p.color,
                        radius = p.size,
                        center = Offset(p.x * w, p.y * h)
                    )
                }
            }
        }
    }
}
