package com.briefclock.app

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.briefclock.app.audio.AudioRecorderHelper
import com.briefclock.app.data.BriefClockDatabase
import com.briefclock.app.ui.components.AppIcons
import com.briefclock.app.ui.components.SettingsDialog
import com.briefclock.app.ui.screens.BriefAlarmScreen
import com.briefclock.app.ui.screens.NapRouletteScreen
import com.briefclock.app.ui.screens.NapRouletteSession
import com.briefclock.app.ui.screens.StatisticsScreen
import com.briefclock.app.ui.theme.BriefClockTheme
import com.briefclock.app.ui.theme.ThemePreferences

enum class BottomTab(val index: Int) {
    ALARM(0),
    ROULETTE(1),
    STATISTICS(2)
}

class MainActivity : ComponentActivity() {

    private lateinit var database: BriefClockDatabase
    private lateinit var audioRecorderHelper: AudioRecorderHelper
    private lateinit var themePreferences: ThemePreferences
    private val rouletteSession = NapRouletteSession()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = BriefClockDatabase.getInstance(this)
        audioRecorderHelper = AudioRecorderHelper(this)
        themePreferences = ThemePreferences(this)

        // Apply saved language before UI initialization
        ThemePreferences.applyLanguage(this, themePreferences.language)

        setContent {
            var themeMode by remember { mutableStateOf(themePreferences.themeMode) }
            var themeColor by remember { mutableStateOf(themePreferences.themeColor) }
            var appLanguage by remember { mutableStateOf(themePreferences.language) }
            var showSettingsDialog by remember { mutableStateOf(false) }

            val currentSysConfig = LocalConfiguration.current
            val localizedConfig = remember(appLanguage, currentSysConfig) {
                Configuration(currentSysConfig).apply {
                    setLocale(ThemePreferences.getLocaleForLanguage(appLanguage))
                }
            }

            CompositionLocalProvider(LocalConfiguration provides localizedConfig) {
                BriefClockTheme(
                    themeMode = themeMode,
                    themeColor = themeColor
                ) {
                    val context = LocalContext.current
                    var selectedTab by remember { mutableStateOf(BottomTab.ALARM) }

                    // Notification permission for Android 13+
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
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 3.dp
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
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )

                                NavigationBarItem(
                                    selected = selectedTab == BottomTab.ROULETTE,
                                    onClick = { selectedTab = BottomTab.ROULETTE },
                                    icon = {
                                        Icon(
                                            AppIcons.Revolver,
                                            contentDescription = stringResource(R.string.nav_roulette),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    label = { Text(stringResource(R.string.nav_roulette)) },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    audioRecorderHelper = audioRecorderHelper,
                                    onOpenSettings = { showSettingsDialog = true }
                                )
                                BottomTab.ROULETTE -> NapRouletteScreen(
                                    context = context,
                                    session = rouletteSession,
                                    database = database,
                                    onOpenSettings = { showSettingsDialog = true }
                                )
                                BottomTab.STATISTICS -> StatisticsScreen(
                                    context = context,
                                    database = database,
                                    onOpenSettings = { showSettingsDialog = true }
                                )
                            }
                        }
                    }

                    if (showSettingsDialog) {
                        SettingsDialog(
                            currentThemeMode = themeMode,
                            currentThemeColor = themeColor,
                            currentLanguage = appLanguage,
                            onThemeModeChange = { newMode ->
                                themeMode = newMode
                                themePreferences.themeMode = newMode
                            },
                            onThemeColorChange = { newColor ->
                                themeColor = newColor
                                themePreferences.themeColor = newColor
                            },
                            onLanguageChange = { newLang ->
                                appLanguage = newLang
                                themePreferences.language = newLang
                                ThemePreferences.applyLanguage(this@MainActivity, newLang)
                            },
                            onDismiss = { showSettingsDialog = false }
                        )
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
