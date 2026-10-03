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
    var confettiAnimTick by remember { mutableLongStateOf(0L) }

    // Psychedelic warp wave animation phase
    var ringPhase by remember { mutableFloatStateOf(0f) }

    // Completely random colors with various hues, saturations, and brightness
    val psychedelicColors = remember {
        List(16) {
            val hue = Random.nextFloat() * 360f
            val sat = Random.nextFloat() * 0.5f + 0.5f // 0.5 - 1.0
            val bri = Random.nextFloat() * 0.5f + 0.5f // 0.5 - 1.0
            Color.hsv(hue, sat, bri)
        }
    }

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

            // Speed multiplier: starts at 0.5x (halved initial speed), accelerates to 1.5x (3x initial speed!)
            val progress = (10f - timeLeftSec) / 10f
            val currentSpeedMultiplier = 0.5f + progress * 1.0f // 0.5x -> 1.5x (3x of initial)

            // Advance psychedelic ring expansion phase
            ringPhase += dt * 0.28f * currentSpeedMultiplier

            // Spawn new emojis continuously
            spawnTimer += dt
            val spawnInterval = (0.24f / (1f + progress * 0.8f)).coerceAtLeast(0.12f)
            if (spawnTimer >= spawnInterval) {
                spawnTimer = 0f
                val candidateEmojis = listOf("😴", "💤", "🛌")
                val angle = Random.nextFloat() * 2f * PI.toFloat()
                val speed = (Random.nextFloat() * 160f + 120f) * (1f + progress * 0.5f) * 0.5f
                val fadeDur = Random.nextFloat() * 0.3f + 0.15f // 0.15s - 0.45s smooth fade

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

        // Spawn confetti celebration fireworks
        val confettiPalette = listOf(
            Color(0xFFFF007F), Color(0xFF00F0FF), Color(0xFF76FF03),
            Color(0xFFFFD600), Color(0xFFFF6D00), Color(0xFF7B1FA2),
            Color(0xFFFF1744), Color(0xFF00E676), Color(0xFFFFEA00)
        )
        for (i in 0..180) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = Random.nextFloat() * 16f + 4f
            confettiList.add(
                ConfettiParticle(
                    x = 0.5f + (Random.nextFloat() - 0.5f) * 0.2f,
                    y = 0.45f + (Random.nextFloat() - 0.5f) * 0.2f,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 6f, // Initial upward burst
                    color = confettiPalette.random(),
                    size = Random.nextFloat() * 9f + 6f
                )
            )
        }

        // Confetti physics loop
        while (true) {
            delay(16)
            confettiList.forEach { p ->
                p.x += p.vx / 380f
                p.y += p.vy / 750f
                p.vy += 0.28f // gravity
                p.vx *= 0.99f // air resistance
            }
            confettiAnimTick++
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
        // 1. Organic Psychedelic Color Rings (数量减半至4个，极强模糊扩散效果，完全随机高饱和颜色与亮度)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val maxR = hypot(cx, cy) * 1.25f

            // Reduced ring count by half (4 rings instead of 8)
            val ringCount = 4
            val numPoints = 40
            val wobblePhase = ringPhase * 2f

            for (r in 0 until ringCount) {
                val p = (ringPhase + r.toFloat() / ringCount) % 1f
                val radius = (p * p) * maxR // Perspective expansion
                val baseAlpha = (sin(p * PI.toFloat())).coerceIn(0f, 1f) * 0.48f
                val baseStrokeW = (p * 70f + 24f) * density // Extra thick stroke

                val path = Path()
                for (i in 0 until numPoints) {
                    val theta = i * (2f * PI.toFloat() / numPoints)
                    // Organic irregular psychedelic wobble
                    val wobble = 1f + 0.14f * sin(3f * theta + wobblePhase + r) + 0.08f * cos(5f * theta - wobblePhase)
                    val rEff = radius * wobble
                    val px = cx + rEff * cos(theta)
                    val py = cy + rEff * sin(theta)
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()

                val ringColor = psychedelicColors[(r + (ringPhase * 4).toInt()).mod(psychedelicColors.size)]

                // Very strong soft blur diffusion effect by layering multi-step graduated wide strokes
                val blurSteps = 5
                for (step in blurSteps downTo 1) {
                    val expandFactor = 1f + (step - 1) * 0.45f // expands stroke up to 2.8x
                    val stepAlpha = (baseAlpha / (step * 0.8f)).coerceIn(0f, 1f)

                    drawPath(
                        path = path,
                        color = ringColor.copy(alpha = stepAlpha),
                        style = Stroke(width = baseStrokeW * expandFactor)
                    )
                }
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

        // 3. Top HUD: Real-time hit score and countdown timer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
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

        // 4. Results Dialog and Confetti Celebration
        // The celebration confetti fireworks is placed IN FRONT OF the results popup!
        if (isGameOver) {
            // Semi-transparent backdrop scrim and result card
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "🎉 " + stringResource(R.string.easter_egg_result_title),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = String.format(stringResource(R.string.easter_egg_score), score),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format(stringResource(R.string.easter_egg_best), bestScore),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        // Big, centered confirmation button ("太棒了")
                        Button(
                            onClick = onDismissGame,
                            shape = RoundedCornerShape(percent = 50),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
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
            }

            // Confetti Fireworks Layer explicitly rendered IN FRONT OF the Dialog card!
            if (confettiList.isNotEmpty()) {
                val tick = confettiAnimTick // Subscribes Canvas to every animation tick at 60 FPS
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    confettiList.forEach { p ->
                        if (p.y in -0.1f..1.15f && p.x in -0.1f..1.15f) {
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
    }
}
