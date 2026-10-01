package com.briefclock.app.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.briefclock.app.R
import com.briefclock.app.data.BriefClockDatabase
import com.briefclock.app.model.NapRecord
import com.briefclock.app.model.NapStatistics
import com.briefclock.app.ui.components.SponsorDialog
import com.briefclock.app.ui.components.SurvivalRateDonutChart
import com.briefclock.app.ui.components.WeeklyNapBarChart
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatisticsScreen(
    context: Context = LocalContext.current,
    database: BriefClockDatabase,
    onOpenSettings: () -> Unit = {}
) {
    var stats by remember { mutableStateOf(NapStatistics()) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showSponsorDialog by remember { mutableStateOf(false) }

    fun refreshStats() {
        stats = database.getNapStatistics()
    }

    LaunchedEffect(Unit) {
        refreshStats()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.stats_title),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.stats_subtitle),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pink Heart Sponsor Button
                    IconButton(onClick = { showSponsorDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = stringResource(R.string.sponsor_title),
                            tint = Color(0xFFEC4899)
                        )
                    }

                    if (stats.totalGames > 0) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.stats_clear_all),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_title),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Donut Chart & Overview KPI Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    SurvivalRateDonutChart(
                        successCount = stats.successCount,
                        failCount = stats.failCount,
                        winRate = stats.winRatePercent
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatRow(
                            label = stringResource(R.string.stats_total_games),
                            value = "${stats.totalGames}"
                        )
                        StatRow(
                            label = stringResource(R.string.stats_survived),
                            value = "${stats.successCount}",
                            valueColor = Color(0xFF10B981)
                        )
                        StatRow(
                            label = stringResource(R.string.stats_shot),
                            value = "${stats.failCount}",
                            valueColor = MaterialTheme.colorScheme.error
                        )
                        StatRow(
                            label = stringResource(R.string.stats_current_streak),
                            value = "${stats.currentStreak} 🔥"
                        )
                    }
                }
            }
        }

        // 7-Day Nap Chart Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = stringResource(R.string.stats_7day_chart),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    WeeklyNapBarChart(dailyData = stats.recentDays)
                }
            }
        }

        // Secondary Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.stats_total_sleep),
                    value = "${stats.totalDurationMinutes}m"
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.stats_avg_sleep),
                    value = "${stats.avgDurationMinutes}m"
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.stats_best_streak),
                    value = "${stats.bestStreak} 🏆"
                )
            }
        }

        // Recent History Section Header
        if (stats.recentRecords.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.stats_recent_history),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            items(stats.recentRecords) { record ->
                HistoryItem(record = record)
            }
        }
    }

    // Clear confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.stats_clear_title)) },
            text = { Text(stringResource(R.string.stats_clear_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        database.clearNapRecords()
                        refreshStats()
                        showClearDialog = false
                    }
                ) {
                    Text(stringResource(R.string.stats_clear_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.alarm_cancel))
                }
            }
        )
    }

    // Sponsor Dialog
    if (showSponsorDialog) {
        SponsorDialog(onDismiss = { showSponsorDialog = false })
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun HistoryItem(record: NapRecord) {
    val dateStr = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(record.timestamp))

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = dateStr,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${stringResource(R.string.stats_target)}: ${record.targetDurationSec / 60}m  |  ${stringResource(R.string.stats_actual)}: ${record.actualDurationSec / 60}m ${record.actualDurationSec % 60}s",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (record.isSuccess) Color(0xFF10B981).copy(alpha = 0.18f) else MaterialTheme.colorScheme.error.copy(alpha = 0.18f)
            ) {
                Text(
                    text = if (record.isSuccess) stringResource(R.string.stats_survived) else stringResource(R.string.stats_shot),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (record.isSuccess) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
