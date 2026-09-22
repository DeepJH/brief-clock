package com.briefclock.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BriefAlarmScreen(
    context: Context = LocalContext.current,
    database: BriefClockDatabase,
    audioRecorderHelper: AudioRecorderHelper
) {
    var alarms by remember { mutableStateOf(emptyList<AlarmItem>()) }
    val coroutineScope = rememberCoroutineScope()

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
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val cal = Calendar.getInstance()
                    selectedHour = cal.get(Calendar.HOUR_OF_DAY)
                    selectedMinute = (cal.get(Calendar.MINUTE) + 5) % 60
                    alarmLabel = ""
                    recordedFile = null
                    showAddManualDialog = true
                },
                containerColor = Color(0xFFE53935),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.alarm_add_regular))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Screen Header: Current Time Display
            var currentTimeStr by remember { mutableStateOf("") }
            LaunchedEffect(Unit) {
                while (true) {
                    currentTimeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                    delay(1000)
                }
            }

            Text(
                text = currentTimeStr,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.alarm_record_prompt),
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            // Hold-to-Record Card Button
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isHoldingToRecord) Color(0xFFD32F2F) else Color(0xFF1E2228)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(54.dp)
                            .scale(if (isHoldingToRecord) pulseScale else 1f)
                            .background(
                                color = if (isHoldingToRecord) Color.White else Color(0xFFE53935),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = com.briefclock.app.ui.components.AppIcons.Mic,
                            contentDescription = null,
                            tint = if (isHoldingToRecord) Color(0xFFE53935) else Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = if (isHoldingToRecord) {
                                "${stringResource(R.string.alarm_recording)} (${recordingDurationSec}s)"
                            } else {
                                stringResource(R.string.alarm_hold_to_record)
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (isHoldingToRecord) "● REC" else "Release to set alarm ringtone",
                            fontSize = 12.sp,
                            color = if (isHoldingToRecord) Color(0xFFFFCDD2) else Color.LightGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Alarm List
            if (alarms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.alarm_no_alarms),
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                if (!alarm.audioPath.isNullOrEmpty()) {
                                    val f = File(alarm.audioPath)
                                    if (audioRecorderHelper.isPlaying()) {
                                        audioRecorderHelper.stopPreview()
                                    } else {
                                        audioRecorderHelper.previewAudio(f)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Set Voice Alarm Dialog
    if (showSetDialog && recordedFile != null) {
        AlertDialog(
            onDismissRequest = {
                audioRecorderHelper.stopPreview()
                showSetDialog = false
            },
            title = {
                Text(stringResource(R.string.alarm_quick_set_title), fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Audio preview player & re-record
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF262B33)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(
                                onClick = {
                                    if (isPreviewPlaying) {
                                        audioRecorderHelper.stopPreview()
                                        isPreviewPlaying = false
                                    } else {
                                        isPreviewPlaying = true
                                        audioRecorderHelper.previewAudio(recordedFile!!) {
                                            isPreviewPlaying = false
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isPreviewPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isPreviewPlaying) stringResource(R.string.alarm_stop_preview) else stringResource(R.string.alarm_preview_voice))
                            }

                            TextButton(
                                onClick = {
                                    audioRecorderHelper.stopPreview()
                                    isPreviewPlaying = false
                                    showSetDialog = false
                                }
                            ) {
                                Text(stringResource(R.string.alarm_rerecord), color = Color(0xFFFF8A80))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Time Picker (Hour & Minute Steppers)
                    Text(
                        stringResource(R.string.alarm_time),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TimeSelectorRow(
                        hour = selectedHour,
                        minute = selectedMinute,
                        onTimeChange = { h, m ->
                            selectedHour = h
                            selectedMinute = m
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Label
                    OutlinedTextField(
                        value = alarmLabel,
                        onValueChange = { alarmLabel = it },
                        label = { Text(stringResource(R.string.alarm_label)) },
                        placeholder = { Text(stringResource(R.string.alarm_label_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        audioRecorderHelper.stopPreview()
                        val newAlarm = AlarmItem(
                            hour = selectedHour,
                            minute = selectedMinute,
                            label = alarmLabel.ifEmpty { context.getString(R.string.alarm_voice_tag) },
                            isEnabled = true,
                            audioPath = recordedFile?.absolutePath,
                            isSystemAlarm = true
                        )
                        val id = database.insertAlarm(newAlarm)
                        val inserted = newAlarm.copy(id = id)
                        AlarmScheduler.scheduleAlarm(context, inserted)
                        Toast.makeText(context, R.string.alarm_system_invoked, Toast.LENGTH_SHORT).show()
                        refreshAlarms()
                        showSetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text(stringResource(R.string.alarm_save), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        audioRecorderHelper.stopPreview()
                        showSetDialog = false
                    }
                ) {
                    Text(stringResource(R.string.alarm_cancel))
                }
            }
        )
    }

    // Manual Add Dialog
    if (showAddManualDialog) {
        AlertDialog(
            onDismissRequest = { showAddManualDialog = false },
            title = { Text(stringResource(R.string.alarm_add_regular), fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        stringResource(R.string.alarm_time),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TimeSelectorRow(
                        hour = selectedHour,
                        minute = selectedMinute,
                        onTimeChange = { h, m ->
                            selectedHour = h
                            selectedMinute = m
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = alarmLabel,
                        onValueChange = { alarmLabel = it },
                        label = { Text(stringResource(R.string.alarm_label)) },
                        placeholder = { Text(stringResource(R.string.alarm_label_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newAlarm = AlarmItem(
                            hour = selectedHour,
                            minute = selectedMinute,
                            label = alarmLabel.ifEmpty { context.getString(R.string.alarm_title) },
                            isEnabled = true,
                            audioPath = null,
                            isSystemAlarm = true
                        )
                        val id = database.insertAlarm(newAlarm)
                        val inserted = newAlarm.copy(id = id)
                        AlarmScheduler.scheduleAlarm(context, inserted)
                        Toast.makeText(context, R.string.alarm_system_invoked, Toast.LENGTH_SHORT).show()
                        refreshAlarms()
                        showAddManualDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text(stringResource(R.string.alarm_save), color = Color.White)
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
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2228)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alarm.formattedTime,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (alarm.isEnabled) Color.White else Color.Gray
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = alarm.label.ifEmpty { stringResource(R.string.alarm_title) },
                        fontSize = 14.sp,
                        color = Color.LightGray
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
                            }
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFE53935)
                    )
                )

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.alarm_delete),
                        tint = Color(0xFF888888)
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
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            IconButton(onClick = { onTimeChange((hour + 23) % 24, minute) }) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Hour Down")
            }
        }

        Text(
            text = ":",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Minute Stepper
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = { onTimeChange(hour, (minute + 1) % 60) }) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Minute Up")
            }
            Text(
                text = String.format("%02d", minute),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            IconButton(onClick = { onTimeChange(hour, (minute + 59) % 60) }) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minute Down")
            }
        }
    }
}
