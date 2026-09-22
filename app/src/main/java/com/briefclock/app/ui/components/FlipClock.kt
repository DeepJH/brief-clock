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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
        FlipCard(value = hours, label = "HR")

        Text(
            text = ":",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 6.dp)
        )

        FlipCard(value = minutes, label = "MIN")

        Text(
            text = ":",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 6.dp)
        )

        FlipCard(value = seconds, label = "SEC", isSeconds = true)
    }
}

@Composable
private fun FlipCard(
    value: Int,
    label: String,
    isSeconds: Boolean = false
) {
    var currentValue by remember { mutableIntStateOf(value) }
    var previousValue by remember { mutableIntStateOf(value) }
    val flipProgress = remember { Animatable(0f) }

    LaunchedEffect(value) {
        if (value != currentValue) {
            previousValue = currentValue
            currentValue = value
            flipProgress.snapTo(0f)
            flipProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            )
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val formattedNumber = String.format("%02d", currentValue)
        val formattedPrev = String.format("%02d", previousValue)

        // Card Container
        Box(
            modifier = Modifier
                .width(if (isSeconds) 72.dp else 84.dp)
                .height(68.dp)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF242A35),
                            Color(0xFF181C24)
                        )
                    )
                )
                .border(1.dp, Color(0xFF384050), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Underneath/Next Number Display
            Text(
                text = formattedNumber,
                fontSize = if (isSeconds) 34.sp else 38.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = if (isSeconds) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
            )

            // Flipping Flap Overlay (Rotates downward from middle seam)
            if (flipProgress.value in 0.01f..0.99f) {
                val rotX = flipProgress.value * 180f
                val showingPrev = rotX < 90f
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationX = rotX
                            cameraDistance = 12f * density
                            transformOrigin = TransformOrigin(0.5f, 0.5f)
                        }
                        .background(Color(0xFF1F242D).copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (showingPrev) formattedPrev else formattedNumber,
                        fontSize = if (isSeconds) 34.sp else 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            // Mechanical Horizontal Split Line / Seam
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(Color(0xFF0F1216))
                    .align(Alignment.Center)
            )

            // Left Hinge Rivet
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF64748B))
                    .align(Alignment.CenterStart)
            )

            // Right Hinge Rivet
            Box(
                modifier = Modifier
                    .size(4.dp)
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
