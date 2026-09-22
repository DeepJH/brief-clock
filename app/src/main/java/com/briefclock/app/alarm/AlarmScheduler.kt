package com.briefclock.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import androidx.core.content.FileProvider
import com.briefclock.app.MainActivity
import com.briefclock.app.model.AlarmItem
import java.io.File
import java.util.Calendar

object AlarmScheduler {

    const val EXTRA_ALARM_ID = "extra_alarm_id"
    const val EXTRA_ALARM_LABEL = "extra_alarm_label"
    const val EXTRA_ALARM_AUDIO_PATH = "extra_alarm_audio_path"
    const val ACTION_ALARM_TRIGGER = "com.briefclock.app.ACTION_ALARM_TRIGGER"

    fun scheduleAlarm(context: Context, alarm: AlarmItem): Long {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return 0L

        val triggerTime = calculateTriggerTime(alarm)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGER
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_ALARM_LABEL, alarm.label)
            putExtra(EXTRA_ALARM_AUDIO_PATH, alarm.audioPath)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            alarm.id.toInt(),
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val clockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
        alarmManager.setAlarmClock(clockInfo, pendingIntent)

        // Also invoke system alarm clock if possible
        invokeSystemAlarmClock(context, alarm)

        return triggerTime
    }

    fun cancelAlarm(context: Context, alarmId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun scheduleSnooze(context: Context, alarmId: Long, label: String, audioPath: String?, snoozeMinutes: Int = 5) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerTime = System.currentTimeMillis() + snoozeMinutes * 60 * 1000L

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGER
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_LABEL, label)
            putExtra(EXTRA_ALARM_AUDIO_PATH, audioPath)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (alarmId + 9999).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            alarmId.toInt(),
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val clockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
        alarmManager.setAlarmClock(clockInfo, pendingIntent)
    }

    fun calculateTriggerTime(alarm: AlarmItem): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (alarm.daysOfWeek == 0) {
            // Once: if time already passed today, set for tomorrow
            if (target.timeInMillis <= now.timeInMillis) {
                target.add(Calendar.DAY_OF_YEAR, 1)
            }
            return target.timeInMillis
        }

        // Repeat days: find the next matching day
        // Calendar.DAY_OF_WEEK: Sunday=1, Monday=2, ..., Saturday=7
        // Our bitmask: Mon=1, Tue=2, Wed=4, Thu=8, Fri=16, Sat=32, Sun=64
        for (dayOffset in 0..7) {
            val candidate = Calendar.getInstance().apply {
                timeInMillis = target.timeInMillis
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }
            if (candidate.timeInMillis <= now.timeInMillis) continue

            val dayOfWeek = candidate.get(Calendar.DAY_OF_WEEK)
            val bitIndex = when (dayOfWeek) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }

            if (alarm.isDaySelected(bitIndex)) {
                return candidate.timeInMillis
            }
        }

        // Fallback: tomorrow
        target.add(Calendar.DAY_OF_YEAR, 1)
        return target.timeInMillis
    }

    private fun invokeSystemAlarmClock(context: Context, alarm: AlarmItem) {
        try {
            val systemIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, alarm.hour)
                putExtra(AlarmClock.EXTRA_MINUTES, alarm.minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, alarm.label.ifEmpty { "Brief Alarm" })
                putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                putExtra(AlarmClock.EXTRA_VIBRATE, true)

                // If custom audio recording exists, pass its URI as ringtone
                if (!alarm.audioPath.isNullOrEmpty()) {
                    val audioFile = File(alarm.audioPath)
                    if (audioFile.exists()) {
                        val contentUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            audioFile
                        )
                        putExtra(AlarmClock.EXTRA_RINGTONE, contentUri.toString())
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (systemIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(systemIntent)
            }
        } catch (e: Exception) {
            // Some OEM ROMs or simulators lack standard AlarmClock receiver
            e.printStackTrace()
        }
    }
}
