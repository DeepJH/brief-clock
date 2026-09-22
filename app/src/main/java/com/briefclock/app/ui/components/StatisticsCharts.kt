package com.briefclock.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.briefclock.app.model.DailyNapDuration

@Composable
fun SurvivalRateDonutChart(
    successCount: Int,
    failCount: Int,
    winRate: Float,
    modifier: Modifier = Modifier
) {
    val total = successCount + failCount
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(winRate) {
        animatedProgress.animateTo(
            targetValue = if (total > 0) winRate / 100f else 0f,
            animationSpec = tween(1000)
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(150.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val strokeWidth = 18.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)

            // Background track
            drawArc(
                color = Color(0xFF2A2E33),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )

            if (total > 0) {
                // Fail segment (red)
                drawArc(
                    color = Color(0xFFE53935),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Win segment (green)
                val successSweep = 360f * animatedProgress.value
                if (successSweep > 0f) {
                    drawArc(
                        color = Color(0xFF00E676),
                        startAngle = -90f,
                        sweepAngle = successSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (total > 0) "${(animatedProgress.value * 100).toInt()}%" else "--",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = if (total > 0 && winRate >= 50f) Color(0xFF00E676) else Color(0xFFFF5252)
            )
            Text(
                text = "$successCount / $total",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun WeeklyNapBarChart(
    dailyData: List<DailyNapDuration>,
    modifier: Modifier = Modifier
) {
    if (dailyData.isEmpty()) return

    val maxMinutes = (dailyData.maxOfOrNull { it.totalMinutes } ?: 0).coerceAtLeast(30)
    val animatedFraction = remember { Animatable(0f) }

    LaunchedEffect(dailyData) {
        animatedFraction.animateTo(1f, animationSpec = tween(900))
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        val width = size.width
        val height = size.height
        val bottomY = height - 28.dp.toPx()
        val chartHeight = bottomY - 24.dp.toPx()

        // Draw baseline
        drawLine(
            color = Color(0xFF374151),
            start = Offset(0f, bottomY),
            end = Offset(width, bottomY),
            strokeWidth = 1.5f
        )

        val barCount = dailyData.size
        val barSlotWidth = width / barCount
        val barWidth = barSlotWidth * 0.52f

        dailyData.forEachIndexed { index, day ->
            val centerX = (index * barSlotWidth) + (barSlotWidth / 2f)
            val barHeight = ((day.totalMinutes.toFloat() / maxMinutes) * chartHeight * animatedFraction.value).coerceAtLeast(if (day.totalMinutes > 0) 6f else 0f)
            val barTop = bottomY - barHeight

            val barColor = when {
                day.totalMinutes == 0 -> Color(0xFF262A30)
                day.failCount > 0 -> Color(0xFFFF5252)
                else -> Color(0xFF00E676)
            }

            // Draw bar
            if (barHeight > 0f) {
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(centerX - barWidth / 2f, barTop),
                    size = Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }

            // Draw text using native android Canvas paint
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 10.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }

            // Date label below bar
            drawContext.canvas.nativeCanvas.drawText(
                day.dateLabel,
                centerX,
                height - 6.dp.toPx(),
                paint
            )

            // Value text above bar if > 0
            if (day.totalMinutes > 0 && animatedFraction.value > 0.8f) {
                val valuePaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 10.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                drawContext.canvas.nativeCanvas.drawText(
                    "${day.totalMinutes}m",
                    centerX,
                    (barTop - 4.dp.toPx()).coerceAtLeast(14.dp.toPx()),
                    valuePaint
                )
            }
        }
    }
}
