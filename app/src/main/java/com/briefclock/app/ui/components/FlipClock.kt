package com.briefclock.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun FlipClock(
    modifier: Modifier = Modifier
) {
    var hours by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var minutes by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MINUTE)) }
    var seconds by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.SECOND)) }

    LaunchedEffect(Unit) {
        while (true) {
            val cal = Calendar.getInstance()
            hours = cal.get(Calendar.HOUR_OF_DAY)
            minutes = cal.get(Calendar.MINUTE)
            seconds = cal.get(Calendar.SECOND)
            delay(500)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MechanicalFlipCard(value = hours, label = "HOUR")

        Text(
            text = ":",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 6.dp)
        )

        MechanicalFlipCard(value = minutes, label = "MIN")

        Text(
            text = ":",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 6.dp)
        )

        MechanicalFlipCard(value = seconds, label = "SEC", isSeconds = true)
    }
}

/**
 * Authentic Solari Split-Flap mechanical digit component.
 * Features 4 half-panels:
 * - Back Top (reveals new digit top)
 * - Back Bottom (holds old digit bottom until covered)
 * - Front Upper Flap (rotates forward 0° -> 90° with old digit top)
 * - Front Lower Flap (rotates into place -90° -> 0° with new digit bottom)
 */
@Composable
fun MechanicalFlipCard(
    value: Int,
    label: String,
    isSeconds: Boolean = false,
    cardWidth: Dp = if (isSeconds) 72.dp else 82.dp,
    cardHeight: Dp = 72.dp,
    fontSize: TextUnit = if (isSeconds) 34.sp else 38.sp
) {
    var currentDisplayVal by remember { mutableIntStateOf(value) }
    var previousDisplayVal by remember { mutableIntStateOf(value) }
    val flipProgress = remember { Animatable(1f) }

    LaunchedEffect(value) {
        if (value != currentDisplayVal) {
            previousDisplayVal = currentDisplayVal
            currentDisplayVal = value
            flipProgress.snapTo(0f)
            flipProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing)
            )
        }
    }

    val oldStr = String.format("%02d", previousDisplayVal)
    val newStr = String.format("%02d", currentDisplayVal)
    val isAnimating = flipProgress.value < 1f
    val progress = flipProgress.value

    val halfHeight = cardHeight / 2
    val cardBg = Color(0xFF1E222A)
    val textColor = if (isSeconds) MaterialTheme.colorScheme.primary else Color(0xFFF1F5F9)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(width = cardWidth, height = cardHeight)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFF333B47), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            // 1. Stationary Background Cards
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Background Top Half (Shows NEW digit top if animating, else current)
                FlapTopHalf(
                    text = if (isAnimating) newStr else newStr,
                    cardWidth = cardWidth,
                    totalHeight = cardHeight,
                    backgroundColor = cardBg,
                    textColor = textColor,
                    fontSize = fontSize
                )

                // Background Bottom Half (Shows OLD digit bottom if animating, else current)
                FlapBottomHalf(
                    text = if (isAnimating) oldStr else newStr,
                    cardWidth = cardWidth,
                    totalHeight = cardHeight,
                    backgroundColor = cardBg,
                    textColor = textColor,
                    fontSize = fontSize
                )
            }

            // 2. Animated Flipping Flaps
            if (isAnimating) {
                if (progress <= 0.5f) {
                    // Upper half folding down (0° -> 90°)
                    val rotX = progress * 180f
                    val shadowAlpha = (progress * 1.4f).coerceIn(0f, 0.7f)

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .size(cardWidth, halfHeight)
                            .graphicsLayer {
                                rotationX = rotX
                                cameraDistance = 16f * density
                                transformOrigin = TransformOrigin(0.5f, 1.0f) // Pivot at bottom seam
                            }
                    ) {
                        FlapTopHalf(
                            text = oldStr,
                            cardWidth = cardWidth,
                            totalHeight = cardHeight,
                            backgroundColor = cardBg,
                            textColor = textColor,
                            fontSize = fontSize,
                            shadowAlpha = shadowAlpha
                        )
                    }
                } else {
                    // Lower half unfolding down (-90° -> 0°)
                    val rotX = (progress - 1f) * 180f
                    val shadowAlpha = ((1f - progress) * 1.4f).coerceIn(0f, 0.7f)

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .size(cardWidth, halfHeight)
                            .graphicsLayer {
                                rotationX = rotX
                                cameraDistance = 16f * density
                                transformOrigin = TransformOrigin(0.5f, 0.0f) // Pivot at top seam
                            }
                    ) {
                        FlapBottomHalf(
                            text = newStr,
                            cardWidth = cardWidth,
                            totalHeight = cardHeight,
                            backgroundColor = cardBg,
                            textColor = textColor,
                            fontSize = fontSize,
                            shadowAlpha = shadowAlpha
                        )
                    }
                }
            }

            // 3. Central Divider Slit / Seam
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(Color(0xFF0F1216))
                    .align(Alignment.Center)
            )

            // Side Hinge Rivets
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 6.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF64748B))
                    .align(Alignment.CenterStart)
            )
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 6.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF64748B))
                    .align(Alignment.CenterEnd)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            letterSpacing = 1.2.sp
        )
    }
}

@Composable
private fun FlapTopHalf(
    text: String,
    cardWidth: Dp,
    totalHeight: Dp,
    backgroundColor: Color,
    textColor: Color,
    fontSize: TextUnit,
    shadowAlpha: Float = 0f
) {
    val halfHeight = totalHeight / 2
    Box(
        modifier = Modifier
            .size(cardWidth, halfHeight)
            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.TopCenter
    ) {
        // Full height box aligned at top, text centered
        Box(
            modifier = Modifier.size(cardWidth, totalHeight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }

        if (shadowAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = shadowAlpha))
            )
        }
    }
}

@Composable
private fun FlapBottomHalf(
    text: String,
    cardWidth: Dp,
    totalHeight: Dp,
    backgroundColor: Color,
    textColor: Color,
    fontSize: TextUnit,
    shadowAlpha: Float = 0f
) {
    val halfHeight = totalHeight / 2
    Box(
        modifier = Modifier
            .size(cardWidth, halfHeight)
            .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Full height box aligned at bottom, text centered
        Box(
            modifier = Modifier.size(cardWidth, totalHeight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }

        if (shadowAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = shadowAlpha))
            )
        }
    }
}
