package com.briefclock.app.alarm

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.briefclock.app.BriefClockApplication
import com.briefclock.app.R
import java.io.File

class AlarmAlertService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onCreate() {
        super.onCreate()
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L) ?: -1L
        val label = intent?.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: ""
        val audioPath = intent?.getStringExtra(AlarmScheduler.EXTRA_ALARM_AUDIO_PATH)

        val notification = createNotification(alarmId, label, audioPath)
        startForeground(NOTIFICATION_ID, notification)

        playAlarmAudio(audioPath)
        startVibration()

        return START_NOT_STICKY
    }

    private fun playAlarmAudio(audioPath: String?) {
        stopAudio()
        try {
            val uri: Uri = if (!audioPath.isNullOrEmpty() && File(audioPath).exists()) {
                Uri.fromFile(File(audioPath))
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startVibration() {
        try {
            val pattern = longArrayOf(0, 800, 400, 800, 400)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopAudio() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // ignore
        } finally {
            mediaPlayer = null
        }
    }

    private fun stopVibration() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun createNotification(alarmId: Long, label: String, audioPath: String?): Notification {
        val fullScreenIntent = Intent(this, AlarmAlertActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, label)
            putExtra(AlarmScheduler.EXTRA_ALARM_AUDIO_PATH, audioPath)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, BriefClockApplication.ALARM_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(getString(R.string.alarm_ringing_title))
            .setContentText(label.ifEmpty { getString(R.string.alarm_title) })
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        stopAudio()
        stopVibration()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_DISMISS = "com.briefclock.app.ACTION_DISMISS"

        fun stop(context: Context) {
            context.stopService(Intent(context, AlarmAlertService::class.java))
        }
    }
}
