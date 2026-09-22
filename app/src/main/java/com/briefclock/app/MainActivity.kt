package com.briefclock.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.briefclock.app.audio.AudioRecorderHelper
import com.briefclock.app.data.BriefClockDatabase
import com.briefclock.app.ui.components.AppIcons
import com.briefclock.app.ui.screens.BriefAlarmScreen
import com.briefclock.app.ui.screens.NapRouletteScreen
import com.briefclock.app.ui.screens.NapRouletteSession
import com.briefclock.app.ui.screens.StatisticsScreen
import com.briefclock.app.ui.theme.BriefClockTheme

enum class BottomTab(val index: Int) {
    ALARM(0),
    ROULETTE(1),
    STATISTICS(2)
}

class MainActivity : ComponentActivity() {

    private lateinit var database: BriefClockDatabase
    private lateinit var audioRecorderHelper: AudioRecorderHelper
    private val rouletteSession = NapRouletteSession()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = BriefClockDatabase.getInstance(this)
        audioRecorderHelper = AudioRecorderHelper(this)

        setContent {
            BriefClockTheme {
                val context = LocalContext.current
                var selectedTab by remember { mutableStateOf(BottomTab.ALARM) }

                // Request notification permission for Android 13+
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) {}

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color(0xFF181C22),
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == BottomTab.ALARM,
                                onClick = { selectedTab = BottomTab.ALARM },
                                icon = {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = stringResource(R.string.nav_alarm)
                                    )
                                },
                                label = { Text(stringResource(R.string.nav_alarm)) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = Color.White,
                                    indicatorColor = Color(0xFFE53935),
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray
                                )
                            )

                            NavigationBarItem(
                                selected = selectedTab == BottomTab.ROULETTE,
                                onClick = { selectedTab = BottomTab.ROULETTE },
                                icon = {
                                    Icon(
                                        AppIcons.Revolver,
                                        contentDescription = stringResource(R.string.nav_roulette)
                                    )
                                },
                                label = { Text(stringResource(R.string.nav_roulette)) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = Color.White,
                                    indicatorColor = Color(0xFFE53935),
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray
                                )
                            )

                            NavigationBarItem(
                                selected = selectedTab == BottomTab.STATISTICS,
                                onClick = { selectedTab = BottomTab.STATISTICS },
                                icon = {
                                    Icon(
                                        AppIcons.BarChart,
                                        contentDescription = stringResource(R.string.nav_statistics)
                                    )
                                },
                                label = { Text(stringResource(R.string.nav_statistics)) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = Color.White,
                                    indicatorColor = Color(0xFFE53935),
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray
                                )
                            )
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (selectedTab) {
                            BottomTab.ALARM -> BriefAlarmScreen(
                                context = context,
                                database = database,
                                audioRecorderHelper = audioRecorderHelper
                            )
                            BottomTab.ROULETTE -> NapRouletteScreen(
                                context = context,
                                session = rouletteSession,
                                database = database
                            )
                            BottomTab.STATISTICS -> StatisticsScreen(
                                context = context,
                                database = database
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        audioRecorderHelper.release()
        super.onDestroy()
    }
}
