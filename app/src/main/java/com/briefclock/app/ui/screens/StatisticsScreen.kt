package com.briefclock.app.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.briefclock.app.ui.components.SurvivalRateDonutChart
import com.briefclock.app.ui.components.WeeklyNapBarChart
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatisticsScreen(
    context: Context = LocalContext.current,
    database: BriefClockDatabase
) {
    var stats by remember { mutableStateOf(NapStatistics()) }
    var showClearDialog by remember { mutableStateOf(false) }

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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.stats_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (stats.totalGames > 0) {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.stats_clear_all),
                            tint = Color.Gray
                        )
                    }
                }
            }
        }

        // Donut Chart & Overview KPI Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2228)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SurvivalRateDonutChart(
                        successCount = stats.successCount,
                        failCount = stats.failCount,
                        winRate = stats.winRatePercent
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.stats_survival_rate),
                            fontSize = 13.sp,
                            color = Color.LightGray
                        )
                        Text(
                            text = "${stringResource(R.string.stats_total_games)}: ${stats.totalGames}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${stringResource(R.string.stats_survived)}: ${stats.successCount}",
                            fontSize = 14.sp,
                            color = Color(0xFF00E676)
                        )
                        Text(
                            text = "${stringResource(R.string.stats_shot)}: ${stats.failCount}",
                            fontSize = 14.sp,
                            color = Color(0xFFFF5252)
                        )
                    }
                }
            }
        }

        // Streak & Duration Metric Tiles
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTile(
                    title = stringResource(R.string.stats_current_streak),
                    value = "${stats.currentStreak}",
                    subtext = "${stringResource(R.string.stats_best_streak)}: ${stats.bestStreak}",
                    modifier = Modifier.weight(1f)
                )

                MetricTile(
                    title = stringResource(R.string.stats_total_duration),
                    value = "${stats.totalDurationMinutes}m",
                    subtext = "${stringResource(R.string.stats_avg_duration)}: ${stats.avgDurationMinutes}m",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 7 Days Chart
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2228)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.stats_chart_title),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (stats.totalGames == 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.stats_chart_empty),
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    } else {
                        WeeklyNapBarChart(dailyData = stats.recentDays)
                    }
                }
            }
        }

        // Recent History
        item {
            Text(
                text = stringResource(R.string.stats_recent_history),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (stats.recentRecords.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.stats_no_history),
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        } else {
            items(stats.recentRecords, key = { it.id }) { record ->
                HistoryRowItem(record = record)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Clear Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.stats_clear_all)) },
            text = { Text(stringResource(R.string.stats_clear_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        database.clearNapRecords()
                        refreshStats()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text(stringResource(R.string.stats_confirm), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.alarm_cancel))
                }
            }
        )
    }
}

@Composable
private fun MetricTile(
    title: String,
    value: String,
    subtext: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2228)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(text = title, fontSize = 12.sp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtext, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun HistoryRowItem(record: NapRecord) {
    val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    val dateStr = sdf.format(Date(record.timestamp))

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF191D23)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${record.actualDurationSec / 60}m ${record.actualDurationSec % 60}s (Target: ${record.targetDurationSec / 60}m)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color.White
                )
                Text(
                    text = dateStr,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            SuggestionChip(
                onClick = {},
                label = {
                    Text(
                        text = if (record.isSuccess) "SURVIVED" else "SHOT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (record.isSuccess) Color(0xFF00E676) else Color(0xFFFF5252)
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = if (record.isSuccess) Color(0xFF003816) else Color(0xFF3B0D0D)
                )
            )
        }
    }
}
