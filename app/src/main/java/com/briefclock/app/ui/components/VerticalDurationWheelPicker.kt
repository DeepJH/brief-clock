package com.briefclock.app.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class NapDurationOption(
    val seconds: Int,
    val label: String
)

val defaultNapOptions = listOf(
    NapDurationOption(10, "10s (Test)"),
    NapDurationOption(5 * 60, "5 mins"),
    NapDurationOption(10 * 60, "10 mins"),
    NapDurationOption(15 * 60, "15 mins"),
    NapDurationOption(20 * 60, "20 mins"),
    NapDurationOption(25 * 60, "25 mins"),
    NapDurationOption(30 * 60, "30 mins"),
    NapDurationOption(45 * 60, "45 mins"),
    NapDurationOption(60 * 60, "60 mins"),
    NapDurationOption(90 * 60, "90 mins")
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VerticalDurationWheelPicker(
    options: List<NapDurationOption> = defaultNapOptions,
    selectedSeconds: Int,
    onDurationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val itemHeight = 44.dp
    val visibleItemsCount = 3
    val totalHeight = itemHeight * visibleItemsCount

    val initialIndex = remember(options, selectedSeconds) {
        val idx = options.indexOfFirst { it.seconds == selectedSeconds }
        if (idx >= 0) idx else 3 // Default 15m
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // Detect center item
    val centerIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) initialIndex
            else {
                val viewportCenter = layoutInfo.viewportStartOffset + (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
                visibleItems.minByOrNull {
                    kotlin.math.abs((it.offset + it.size / 2) - viewportCenter)
                }?.index?.coerceIn(0, options.size - 1) ?: initialIndex
            }
        }
    }

    var lastHapticIndex by remember { mutableIntStateOf(centerIndex) }

    LaunchedEffect(centerIndex) {
        if (centerIndex in options.indices) {
            val chosen = options[centerIndex].seconds
            if (chosen != selectedSeconds) {
                onDurationSelected(chosen)
            }
            if (centerIndex != lastHapticIndex) {
                lastHapticIndex = centerIndex
                triggerTick(context)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight),
        contentAlignment = Alignment.Center
    ) {
        // Center Selection Highlight Lens / Pill
        Box(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(itemHeight)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
        )

        // Scrollable Items Column
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = itemHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(options) { index, option ->
                val isSelected = index == centerIndex
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option.label,
                        fontSize = if (isSelected) 20.sp else 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        }
                    )
                }
            }
        }

        // Top Fade Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * 0.8f)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            Color.Transparent
                        )
                    )
                )
        )

        // Bottom Fade Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * 0.8f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )
    }
}

private fun triggerTick(context: Context) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(10)
        }
    } catch (_: Exception) {}
}
