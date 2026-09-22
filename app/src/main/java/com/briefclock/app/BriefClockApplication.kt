package com.briefclock.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.briefclock.app.data.BriefClockDatabase

class BriefClockApplication : Application() {

    companion object {
        const val ALARM_CHANNEL_ID = "brief_clock_alarm_channel"
        const val NAP_CHANNEL_ID = "brief_clock_nap_channel"
    }

    override fun onCreate() {
        super.onCreate()
        BriefClockDatabase.getInstance(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                getString(R.string.alarm_title),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Brief Clock Alarm Notifications"
                enableVibration(true)
                setShowBadge(true)
            }

            val napChannel = NotificationChannel(
                NAP_CHANNEL_ID,
                getString(R.string.roulette_title),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Nap Roulette Status Notifications"
            }

            notificationManager?.createNotificationChannel(alarmChannel)
            notificationManager?.createNotificationChannel(napChannel)
        }
    }
}
