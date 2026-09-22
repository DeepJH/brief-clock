package com.briefclock.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.briefclock.app.R
import com.briefclock.app.alarm.AlarmScheduler
import com.briefclock.app.audio.AudioRecorderHelper
import com.briefclock.app.data.BriefClockDatabase
import com.briefclock.app.model.AlarmItem
import com.briefclock.app.ui.components.FlipClock
import kotlinx.coroutines.delay
import java.io.File
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BriefAlarmScreen(
    context: Context = LocalContext.current,
    database: BriefClockDatabase,
    audioRecorderHelper: AudioRecorderHelper,
    onOpenSettings: () -> Unit = {}
) {
    var alarms by remember { mutableStateOf(emptyList<AlarmItem>()) }

    fun refreshAlarms() {
        alarms = database.getAllAlarms()
    }

    LaunchedEffect(Unit) {
        refreshAlarms()
    }

    // Recording State
    var isHoldingToRecord by remember { mutableStateOf(false) }
    var recordingDurationSec by remember { mutableIntStateOf(0) }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var showSetDialog by remember { mutableStateOf(false) }
    var isPreviewPlaying by remember { mutableStateOf(false) }

    // Dialog form state
    var selectedHour by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MINUTE) + 5) }
    var alarmLabel by remember { mutableStateOf("") }

    // Regular Add Dialog
    var showAddManualDialog by remember { mutableStateOf(false) }

    // Permissions
    var hasRecordAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val recordPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasRecordAudioPermission = granted
        if (!granted) {
            Toast.makeText(context, R.string.alarm_permission_record, Toast.LENGTH_SHORT).show()
        }
    }

    // Timer while recording
    LaunchedEffect(isHoldingToRecord) {
        if (isHoldingToRecord) {
            recordingDurationSec = 0
            while (isHoldingToRecord) {
                delay(1000)
                recordingDurationSec++
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "recordingPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: Title (Brief Alert), Subtitle, + Add Manual Alarm, Settings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.alarm_screen_title),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.alarm_screen_subtitle),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Manual Add Alarm Button (Moved here instead of FAB)
                    IconButton(
                        onClick = {
                            val cal = Calendar.getInstance()
                            selectedHour = cal.get(Calendar.HOUR_OF_DAY)
                            selectedMinute = (cal.get(Calendar.MINUTE) + 5) % 60
                            alarmLabel = ""
                            recordedFile = null
                            showAddManualDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = stringResource(R.string.alarm_add_regular),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Settings Button
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_title),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Retro-Modern Flip Clock
            FlipClock()

            Spacer(modifier = Modifier.height(8.dp))

            // Section Header: Active Alerts & Count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.alarm_list_title),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        text = "${alarms.count { it.isEnabled }}/${alarms.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Alarms List
            if (alarms.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.alarm_no_alarms),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.alarm_record_prompt_hint),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    items(alarms, key = { it.id }) { alarm ->
                        AlarmCard(
                            alarm = alarm,
                            onToggle = { isEnabled ->
                                database.setAlarmEnabled(alarm.id, isEnabled)
                                if (isEnabled) {
                                    AlarmScheduler.scheduleAlarm(context, alarm)
                                } else {
                                    AlarmScheduler.cancelAlarm(context, alarm.id)
                                }
                                refreshAlarms()
                            },
                            onDelete = {
                                AlarmScheduler.cancelAlarm(context, alarm.id)
                                database.deleteAlarm(alarm.id)
                                refreshAlarms()
                            },
                            onPlayVoice = {
                                val path = alarm.audioPath
                                if (!path.isNullOrEmpty()) {
                                    audioRecorderHelper.previewAudio(File(path))
                                }
                            }
                        )
                    }
                }
            }
        }

        // Bottom Capsule-Shaped Record Button (Hold to Record Voice Alarm)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(percent = 50),
                color = if (isHoldingToRecord) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                shadowElevation = if (isHoldingToRecord) 10.dp else 4.dp,
                modifier = Modifier
                    .scale(if (isHoldingToRecord) pulseScale else 1f)
                    .height(54.dp)
                    .widthIn(min = 240.dp)
                    .pointerInput(hasRecordAudioPermission) {
                        detectTapGestures(
                            onPress = {
                                if (!hasRecordAudioPermission) {
                                    recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    return@detectTapGestures
                                }

                                isHoldingToRecord = true
                                val file = AudioRecorderHelper.createNewRecordingFile(context)
                                audioRecorderHelper.startRecording(file)

                                tryAwaitRelease()

                                // Released
                                isHoldingToRecord = false
                                val savedFile = audioRecorderHelper.stopRecording()
                                if (savedFile != null && savedFile.exists() && savedFile.length() > 0L) {
                                    recordedFile = savedFile
                                    // Default alarm time to next minute + 1
                                    val now = Calendar.getInstance()
                                    selectedHour = now.get(Calendar.HOUR_OF_DAY)
                                    selectedMinute = (now.get(Calendar.MINUTE) + 1) % 60
                                    alarmLabel = context.getString(R.string.alarm_voice_tag)
                                    showSetDialog = true
                                }
                            }
                        )
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = if (isHoldingToRecord) {
                            "${stringResource(R.string.alarm_recording)} ${recordingDurationSec}s..."
                        } else {
                            stringResource(R.string.alarm_hold_to_record_capsule)
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }

    // Voice Quick-Set Dialog
    if (showSetDialog && recordedFile != null) {
        AlertDialog(
            onDismissRequest = {
                audioRecorderHelper.stopPreview()
                isPreviewPlaying = false
                showSetDialog = false
            },
            title = {
                Text(
                    text = stringResource(R.string.alarm_quick_set_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Voice preview card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (isPreviewPlaying) {
                                            audioRecorderHelper.stopPreview()
                                            isPreviewPlaying = false
                                        } else {
                                            audioRecorderHelper.previewAudio(recordedFile!!) {
                                                isPreviewPlaying = false
                                            }
                                            isPreviewPlaying = true
                                        }
                                    }
                                ) {
                                    Icon(
                                        if (isPreviewPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                                        contentDescription = "Preview",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = "${stringResource(R.string.alarm_recording)} (${recordingDurationSec}s)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            TextButton(
                                onClick = {
                                    audioRecorderHelper.stopPreview()
                                    isPreviewPlaying = false
                                    recordedFile?.delete()
                                    recordedFile = null
                                    showSetDialog = false
                                }
                            ) {
                                Text(stringResource(R.string.alarm_rerecord), color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    TimeSelectorRow(
                        hour = selectedHour,
                        minute = selectedMinute,
                        onTimeChange = { h, m ->
                            selectedHour = h
                            selectedMinute = m
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = alarmLabel,
                        onValueChange = { alarmLabel = it },
                        label = { Text(stringResource(R.string.alarm_label_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        audioRecorderHelper.stopPreview()
                        isPreviewPlaying = false

                        val item = AlarmItem(
                            hour = selectedHour,
                            minute = selectedMinute,
                            label = alarmLabel.ifEmpty { context.getString(R.string.alarm_voice_tag) },
                            isEnabled = true,
                            audioPath = recordedFile?.absolutePath,
                            isSystemAlarm = true
                        )

                        val id = database.insertAlarm(item)
                        val insertedItem = item.copy(id = id)

                        AlarmScheduler.scheduleAlarm(context, insertedItem)
                        refreshAlarms()
                        showSetDialog = false
                        Toast.makeText(context, R.string.alarm_saved_success, Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.alarm_save), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        audioRecorderHelper.stopPreview()
                        isPreviewPlaying = false
                        showSetDialog = false
                    }
                ) {
                    Text(stringResource(R.string.alarm_cancel))
                }
            }
        )
    }

    // Regular Add Alarm Dialog
    if (showAddManualDialog) {
        AlertDialog(
            onDismissRequest = { showAddManualDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.alarm_add_regular),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TimeSelectorRow(
                        hour = selectedHour,
                        minute = selectedMinute,
                        onTimeChange = { h, m ->
                            selectedHour = h
                            selectedMinute = m
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = alarmLabel,
                        onValueChange = { alarmLabel = it },
                        label = { Text(stringResource(R.string.alarm_label_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val item = AlarmItem(
                            hour = selectedHour,
                            minute = selectedMinute,
                            label = alarmLabel.ifEmpty { context.getString(R.string.alarm_title) },
                            isEnabled = true,
                            audioPath = null,
                            isSystemAlarm = true
                        )

                        val id = database.insertAlarm(item)
                        val insertedItem = item.copy(id = id)

                        AlarmScheduler.scheduleAlarm(context, insertedItem)
                        refreshAlarms()
                        showAddManualDialog = false
                        Toast.makeText(context, R.string.alarm_saved_success, Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.alarm_save), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddManualDialog = false }) {
                    Text(stringResource(R.string.alarm_cancel))
                }
            }
        )
    }
}

@Composable
private fun AlarmCard(
    alarm: AlarmItem,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onPlayVoice: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alarm.formattedTime,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (alarm.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = alarm.label.ifEmpty { stringResource(R.string.alarm_title) },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (alarm.isVoiceAlarm) {
                        Spacer(modifier = Modifier.width(8.dp))
                        AssistChip(
                            onClick = onPlayVoice,
                            label = { Text(stringResource(R.string.alarm_voice_tag), fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.alarm_delete),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeSelectorRow(
    hour: Int,
    minute: Int,
    onTimeChange: (Int, Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hour Stepper
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = { onTimeChange((hour + 1) % 24, minute) }) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Hour Up")
            }
            Text(
                text = String.format("%02d", hour),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = { onTimeChange((hour + 23) % 24, minute) }) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Hour Down")
            }
        }

        Text(
            text = ":",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 14.dp)
        )

        // Minute Stepper
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = { onTimeChange(hour, (minute + 1) % 60) }) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Minute Up")
            }
            Text(
                text = String.format("%02d", minute),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = { onTimeChange(hour, (minute + 59) % 60) }) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minute Down")
            }
        }
    }
}
