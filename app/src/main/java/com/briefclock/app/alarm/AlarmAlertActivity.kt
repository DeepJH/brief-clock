package com.briefclock.app.alarm

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.briefclock.app.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmAlertActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        turnScreenOnAndKeyguardDismiss()

        val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
        val label = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: ""
        val audioPath = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_AUDIO_PATH)

        setContent {
            MaterialTheme {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.25f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )

                val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF121212)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Spacer(modifier = Modifier.height(32.dp))

                        // Header Info
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentTime,
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = label.ifEmpty { stringResource(R.string.alarm_ringing_title) },
                                fontSize = 22.sp,
                                color = Color(0xFFFFB74D),
                                fontWeight = FontWeight.SemiBold
                            )
                            if (!audioPath.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(stringResource(R.string.alarm_voice_tag), color = Color.White) }
                                )
                            }
                        }

                        // Pulsing Icon
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(140.dp)
                                .scale(scale)
                                .background(Color(0xFFE53935).copy(alpha = 0.2f), shape = CircleShape)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(Color(0xFFE53935), shape = CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(54.dp)
                                )
                            }
                        }

                        // Action Buttons: Snooze & Dismiss
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = {
                                    AlarmAlertService.stop(this@AlarmAlertActivity)
                                    finish()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.alarm_dismiss),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = {
                                    AlarmAlertService.stop(this@AlarmAlertActivity)
                                    AlarmScheduler.scheduleSnooze(this@AlarmAlertActivity, alarmId, label, audioPath)
                                    finish()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.alarm_snooze),
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }

    private fun turnScreenOnAndKeyguardDismiss() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            km?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }
}
