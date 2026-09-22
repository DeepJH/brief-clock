package com.briefclock.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.briefclock.app.data.BriefClockDatabase

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val alarmId = intent?.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L) ?: -1L
        val label = intent?.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: ""
        val audioPath = intent?.getStringExtra(AlarmScheduler.EXTRA_ALARM_AUDIO_PATH)

        // Reschedule next occurrence if repeat
        if (alarmId > 0) {
            val db = BriefClockDatabase.getInstance(context)
            val alarm = db.getAlarmById(alarmId)
            if (alarm != null) {
                if (alarm.daysOfWeek > 0) {
                    AlarmScheduler.scheduleAlarm(context, alarm)
                } else {
                    // Turn off one-time alarm
                    db.setAlarmEnabled(alarmId, false)
                }
            }
        }

        // Start Alert Service in Foreground
        val serviceIntent = Intent(context, AlarmAlertService::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, label)
            putExtra(AlarmScheduler.EXTRA_ALARM_AUDIO_PATH, audioPath)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // Launch AlarmAlertActivity
        val alertIntent = Intent(context, AlarmAlertActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, label)
            putExtra(AlarmScheduler.EXTRA_ALARM_AUDIO_PATH, audioPath)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(alertIntent)
    }
}
