package com.briefclock.app.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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

data class FlyingEmoji(
    val id: Long,
    val emoji: String,
    val angle: Float,
    val speed: Float,
    val spawnTime: Long,
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
    var timeLeftSec by remember { mutableIntStateOf(10) }
    var isGameOver by remember { mutableStateOf(false) }

    // Best score from SharedPreferences
    val prefs = remember { context.getSharedPreferences("easter_egg_prefs", Context.MODE_PRIVATE) }
    var bestScore by remember { mutableIntStateOf(prefs.getInt("key_best_egg_score", 0)) }

    val emojisList = remember { mutableStateListOf<FlyingEmoji>() }
    var nextEmojiId by remember { mutableLongStateOf(0L) }

    val confettiList = remember { mutableStateListOf<ConfettiParticle>() }

    // 10s Countdown Timer
    LaunchedEffect(Unit) {
        while (timeLeftSec > 0) {
            delay(1000)
            timeLeftSec--
        }
        isGameOver = true

        // Play end gunshot sound and spawn confetti
        SoundEffects.playGunshot(context)
        if (score > bestScore) {
            bestScore = score
            prefs.edit().putInt("key_best_egg_score", bestScore).apply()
        }

        // Spawn confetti fireworks
        val colors = listOf(
            Color(0xFFFF5722), Color(0xFFFFEB3B), Color(0xFF4CAF50),
            Color(0xFF2196F3), Color(0xFFE91E63), Color(0xFF9C27B0)
        )
        for (i in 0..120) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = Random.nextFloat() * 12f + 4f
            confettiList.add(
                ConfettiParticle(
                    x = 0.5f,
                    y = 0.4f,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 5f,
                    color = colors.random(),
                    size = Random.nextFloat() * 8f + 4f
                )
            )
        }
    }

    // Emoji spawner loop
    LaunchedEffect(isGameOver) {
        if (!isGameOver) {
            val candidateEmojis = listOf("😴", "💤", "🛌")
            while (!isGameOver) {
                delay(220)
                val angle = Random.nextFloat() * 2f * PI.toFloat()
                val speed = Random.nextFloat() * 180f + 120f
                emojisList.add(
                    FlyingEmoji(
                        id = nextEmojiId++,
                        emoji = candidateEmojis.random(),
                        angle = angle,
                        speed = speed,
                        spawnTime = System.currentTimeMillis()
                    )
                )

                // Remove emojis that traveled too far (>3.5s)
                val now = System.currentTimeMillis()
                emojisList.removeAll { now - it.spawnTime > 3500L || it.isHit }
            }
        }
    }

    // Infinite Warp Tunnel Animation
    val infiniteTransition = rememberInfiniteTransition(label = "warpTunnel")
    val warpPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080B10))
            .pointerInput(isGameOver) {
                if (!isGameOver) {
                    detectTapGestures { tapOffset ->
                        val now = System.currentTimeMillis()
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()
                        val cx = w / 2f
                        val cy = h / 2f

                        // Check if an emoji was tapped
                        var hitAny = false
                        for (emoji in emojisList) {
                            if (emoji.isHit) continue
                            val elapsed = (now - emoji.spawnTime) / 1000f
                            val dist = emoji.speed * elapsed
                            val ex = cx + cos(emoji.angle) * dist
                            val ey = cy + sin(emoji.angle) * dist
                            val touchRadius = 45.dp.toPx()

                            if (hypot(tapOffset.x - ex, tapOffset.y - ey) <= touchRadius) {
                                emoji.isHit = true
                                hitAny = true
                                score++
                                SoundEffects.playGunshot(context)
                                break
                            }
                        }
                    }
                }
            }
    ) {
        // Warp Tunnel Canvas Background (Expanding concentric rings from center)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val maxR = hypot(cx, cy) * 1.1f

            val ringCount = 8
            for (r in 0 until ringCount) {
                val progress = (warpPhase + r.toFloat() / ringCount) % 1f
                val radius = (progress * progress) * maxR // exponential perspective
                val alpha = (sin(progress * PI.toFloat())).coerceIn(0f, 1f) * 0.45f
                val strokeWidth = (progress * 14f + 2f)

                drawCircle(
                    color = Color(0xFF64748B).copy(alpha = alpha),
                    radius = radius,
                    center = Offset(cx, cy),
                    style = Stroke(width = strokeWidth)
                )
            }
        }

        // Flying Emojis
        val now = System.currentTimeMillis()
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val cx = maxWidth / 2
            val cy = maxHeight / 2

            emojisList.forEach { emoji ->
                if (!emoji.isHit) {
                    val elapsed = (now - emoji.spawnTime) / 1000f
                    val distDp = (emoji.speed * elapsed).dp
                    val ex = cx + (distDp.value * cos(emoji.angle)).dp
                    val ey = cy + (distDp.value * sin(emoji.angle)).dp
                    val scaleFactor = (0.7f + elapsed * 0.9f).coerceIn(0.6f, 2.8f)
                    val fontSize = (24 * scaleFactor).sp

                    Text(
                        text = emoji.emoji,
                        fontSize = fontSize,
                        modifier = Modifier
                            .offset(x = ex - (fontSize.value / 2).dp, y = ey - (fontSize.value / 2).dp)
                    )
                }
            }
        }

        // Confetti Fireworks Layer when game ends
        if (isGameOver && confettiList.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                confettiList.forEach { p ->
                    p.x += p.vx / w
                    p.y += p.vy / h
                    p.vy += 0.2f // gravity
                    drawCircle(
                        color = p.color,
                        radius = p.size,
                        center = Offset(p.x * w, p.y * h)
                    )
                }
            }
        }

        // Top HUD Bar: Countdown & Score Counter
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
                tonalElevation = 4.dp
            ) {
                Text(
                    text = "🎯 击中: $score",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(percent = 50),
                color = if (timeLeftSec <= 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 4.dp
            ) {
                Text(
                    text = "⏳ ${timeLeftSec}s",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (timeLeftSec <= 3) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        // Game Over Dialog
        if (isGameOver) {
            AlertDialog(
                onDismissRequest = onDismissGame,
                title = {
                    Text(
                        text = "🎉 " + stringResource(R.string.easter_egg_result_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = String.format(stringResource(R.string.easter_egg_score), score),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format(stringResource(R.string.easter_egg_best), bestScore),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onDismissGame,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.easter_egg_confirm),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    }
}
