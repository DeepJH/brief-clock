package com.briefclock.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.briefclock.app.R
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
        MechanicalFlipCard(value = hours, label = stringResource(R.string.clock_hour))

        Text(
            text = ":",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 6.dp)
        )

        MechanicalFlipCard(value = minutes, label = stringResource(R.string.clock_minute))

        Text(
            text = ":",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 6.dp)
        )

        MechanicalFlipCard(value = seconds, label = stringResource(R.string.clock_second), isSeconds = true)
    }
}

/**
 * Authentic Solari Split-Flap mechanical digit component.
 * Displays ONE single number split precisely across the middle horizontal seam.
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
    cardHeight: Dp = 74.dp,
    fontSize: TextUnit = if (isSeconds) 34.sp else 38.sp
) {
    var currentDisplayVal by remember { mutableIntStateOf(value) }
    var previousDisplayVal by remember { mutableIntStateOf(value) }
    val flipProgress = remember { Animatable(1f) }

    val textMeasurer = rememberTextMeasurer()

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

    val textStyle = TextStyle(
        fontSize = fontSize,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.Monospace,
        textAlign = TextAlign.Center,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        color = textColor
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(width = cardWidth, height = cardHeight)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFF333B47), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            // 1. Stationary Background Cards (Top shows NEW digit top, Bottom shows OLD digit bottom while animating)
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Background Top Half
                CanvasFlapTopHalf(
                    text = newStr,
                    cardWidth = cardWidth,
                    halfHeight = halfHeight,
                    backgroundColor = cardBg,
                    textStyle = textStyle,
                    textMeasurer = textMeasurer
                )

                // Background Bottom Half
                CanvasFlapBottomHalf(
                    text = if (isAnimating) oldStr else newStr,
                    cardWidth = cardWidth,
                    halfHeight = halfHeight,
                    backgroundColor = cardBg,
                    textStyle = textStyle,
                    textMeasurer = textMeasurer
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
                        CanvasFlapTopHalf(
                            text = oldStr,
                            cardWidth = cardWidth,
                            halfHeight = halfHeight,
                            backgroundColor = cardBg,
                            textStyle = textStyle,
                            textMeasurer = textMeasurer,
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
                        CanvasFlapBottomHalf(
                            text = newStr,
                            cardWidth = cardWidth,
                            halfHeight = halfHeight,
                            backgroundColor = cardBg,
                            textStyle = textStyle,
                            textMeasurer = textMeasurer,
                            shadowAlpha = shadowAlpha
                        )
                    }
                }
            }

            // 3. Central Divider Slit / Seam (1.5dp horizontal mechanical seam)
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

/**
 * Top flap: Canvas clips strictly to top half [0, halfHeight].
 * Positions text center exactly at y = halfHeight (the bottom seam).
 * Guaranteed to draw ONLY the top 50% of the single number!
 */
@Composable
private fun CanvasFlapTopHalf(
    text: String,
    cardWidth: Dp,
    halfHeight: Dp,
    backgroundColor: Color,
    textStyle: TextStyle,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    shadowAlpha: Float = 0f
) {
    Canvas(
        modifier = Modifier
            .size(cardWidth, halfHeight)
            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
    ) {
        // Draw background
        drawRoundRect(
            color = backgroundColor,
            size = size,
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
        )

        // Clip strictly to top half
        clipRect(0f, 0f, size.width, size.height) {
            val measured = textMeasurer.measure(text = text, style = textStyle)
            val tx = (size.width - measured.size.width) / 2f
            val ty = size.height - (measured.size.height / 2f)
            drawText(textMeasurer, text = text, topLeft = Offset(tx, ty), style = textStyle)
        }

        // Shadow overlay during flip
        if (shadowAlpha > 0f) {
            drawRect(color = Color.Black.copy(alpha = shadowAlpha))
        }
    }
}

/**
 * Bottom flap: Canvas clips strictly to bottom half [0, halfHeight].
 * Positions text center exactly at y = 0 (the top seam).
 * Guaranteed to draw ONLY the bottom 50% of the single number!
 */
@Composable
private fun CanvasFlapBottomHalf(
    text: String,
    cardWidth: Dp,
    halfHeight: Dp,
    backgroundColor: Color,
    textStyle: TextStyle,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    shadowAlpha: Float = 0f
) {
    Canvas(
        modifier = Modifier
            .size(cardWidth, halfHeight)
            .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
    ) {
        // Draw background
        drawRoundRect(
            color = backgroundColor,
            size = size,
            cornerRadius = CornerRadius(0f, 0f)
        )

        // Clip strictly to bottom half
        clipRect(0f, 0f, size.width, size.height) {
            val measured = textMeasurer.measure(text = text, style = textStyle)
            val tx = (size.width - measured.size.width) / 2f
            val ty = 0f - (measured.size.height / 2f)
            drawText(textMeasurer, text = text, topLeft = Offset(tx, ty), style = textStyle)
        }

        // Shadow overlay during flip
        if (shadowAlpha > 0f) {
            drawRect(color = Color.Black.copy(alpha = shadowAlpha))
        }
    }
}
