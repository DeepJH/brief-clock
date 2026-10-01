package com.briefclock.app.ui.screens

import android.content.Context
import androidx.compose.animation.core.*
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

        // Play end gunshot sound and record best score
        SoundEffects.playGunshot(context)
        if (score > bestScore) {
            bestScore = score
            prefs.edit().putInt("key_best_egg_score", bestScore).apply()
        }

        // Spawn confetti fireworks in front
        val colors = listOf(
            Color(0xFFFF5722), Color(0xFFFFEB3B), Color(0xFF4CAF50),
            Color(0xFF2196F3), Color(0xFFE91E63), Color(0xFF9C27B0)
        )
        for (i in 0..150) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = Random.nextFloat() * 14f + 5f
            confettiList.add(
                ConfettiParticle(
                    x = 0.5f,
                    y = 0.35f,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 6f,
                    color = colors.random(),
                    size = Random.nextFloat() * 9f + 4f
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
                val speed = Random.nextFloat() * 160f + 110f
                emojisList.add(
                    FlyingEmoji(
                        id = nextEmojiId++,
                        emoji = candidateEmojis.random(),
                        angle = angle,
                        speed = speed,
                        spawnTime = System.currentTimeMillis()
                    )
                )

                // Clean up emojis that traveled out of screen bounds (>3.5s)
                val now = System.currentTimeMillis()
                emojisList.removeAll { now - it.spawnTime > 3500L || it.isHit }
            }
        }
    }

    // Dynamic Warp Tunnel Speed (Starts slow, gets faster over the 10 seconds)
    val elapsedProgress = (10 - timeLeftSec) / 10f
    val baseDurationMs = (1400 - elapsedProgress * 800).toInt().coerceAtLeast(400)

    val infiniteTransition = rememberInfiniteTransition(label = "warpTunnel")
    val warpPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(baseDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Initial background is pure white
            .pointerInput(isGameOver) {
                if (!isGameOver) {
                    detectTapGestures { tapOffset ->
                        val now = System.currentTimeMillis()
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()
                        val cx = w / 2f
                        val cy = h / 2f

                        // Check if an emoji was tapped
                        for (emoji in emojisList) {
                            if (emoji.isHit) continue
                            val elapsed = (now - emoji.spawnTime) / 1000f
                            val dist = emoji.speed * elapsed
                            val ex = cx + cos(emoji.angle) * dist
                            val ey = cy + sin(emoji.angle) * dist
                            val touchRadius = 48.dp.toPx()

                            if (hypot(tapOffset.x - ex, tapOffset.y - ey) <= touchRadius) {
                                emoji.isHit = true
                                score++
                                SoundEffects.playGunshot(context)
                                break
                            }
                        }
                    }
                }
            }
    ) {
        // 1. Soft Blurred Colored Rings Surging Outward from Center (Starts slow, gets faster)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val maxR = hypot(cx, cy) * 1.15f

            val ringCount = 7
            val ringColors = listOf(
                Color(0xFF93C5FD), // Soft baby blue
                Color(0xFFC4B5FD), // Soft lavender
                Color(0xFFFBCFE8), // Soft pastel pink
                Color(0xFFA7F3D0)  // Soft mint
            )

            for (r in 0 until ringCount) {
                val progress = (warpPhase + r.toFloat() / ringCount) % 1f
                val radius = (progress * progress) * maxR
                val alpha = (sin(progress * PI.toFloat())).coerceIn(0f, 1f) * 0.45f
                val strokeWidth = (progress * 22f + 4f)

                drawCircle(
                    color = ringColors[r % ringColors.size].copy(alpha = alpha),
                    radius = radius,
                    center = Offset(cx, cy),
                    style = Stroke(width = strokeWidth)
                )
            }
        }

        // 2. Flying Emojis (Smooth transition from 0% transparent to 100% visible while moving outward)
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

                    // Alpha transitions from 0.0 (completely transparent at center) to 1.0 (fully visible)
                    val alpha = (elapsed / 0.55f).coerceIn(0f, 1f)
                    val scaleFactor = (0.6f + elapsed * 1.0f).coerceIn(0.6f, 2.6f)
                    val fontSize = (26 * scaleFactor).sp

                    Text(
                        text = emoji.emoji,
                        fontSize = fontSize,
                        modifier = Modifier
                            .offset(x = ex - (fontSize.value / 2).dp, y = ey - (fontSize.value / 2).dp)
                            .alpha(alpha)
                    )
                }
            }
        }

        // 3. Confetti Fireworks Layer IN FRONT
        if (isGameOver && confettiList.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                confettiList.forEach { p ->
                    p.x += p.vx / w
                    p.y += p.vy / h
                    p.vy += 0.22f // Gravity pull
                    drawCircle(
                        color = p.color,
                        radius = p.size,
                        center = Offset(p.x * w, p.y * h)
                    )
                }
            }
        }

        // 4. Top HUD Bar: Countdown & Hit Counter
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
                shadowElevation = 4.dp
            ) {
                Text(
                    text = "🎯 击中: $score",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(percent = 50),
                color = if (timeLeftSec <= 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 4.dp
            ) {
                Text(
                    text = "⏳ ${timeLeftSec}s",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (timeLeftSec <= 3) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                )
            }
        }

        // 5. Game Over Celebration Dialog with Centered BIG Button
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
                    // Big centered confirm button ("太棒了")
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
                                .fillMaxWidth(0.85f)
                                .height(54.dp)
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
    }
}
