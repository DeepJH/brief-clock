package com.briefclock.app.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.briefclock.app.model.AlarmItem
import com.briefclock.app.model.DailyNapDuration
import com.briefclock.app.model.NapRecord
import com.briefclock.app.model.NapStatistics
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BriefClockDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "brief_clock.db"
        const val DATABASE_VERSION = 1

        const val TABLE_ALARMS = "alarms"
        const val COL_ALARM_ID = "id"
        const val COL_ALARM_HOUR = "hour"
        const val COL_ALARM_MINUTE = "minute"
        const val COL_ALARM_LABEL = "label"
        const val COL_ALARM_ENABLED = "is_enabled"
        const val COL_ALARM_DAYS = "days_of_week"
        const val COL_ALARM_AUDIO_PATH = "audio_path"
        const val COL_ALARM_SYSTEM = "is_system_alarm"
        const val COL_ALARM_CREATED_AT = "created_at"

        const val TABLE_NAPS = "naps"
        const val COL_NAP_ID = "id"
        const val COL_NAP_TARGET = "target_duration_sec"
        const val COL_NAP_ACTUAL = "actual_duration_sec"
        const val COL_NAP_SUCCESS = "is_success"
        const val COL_NAP_TIMESTAMP = "timestamp"

        @Volatile
        private var instance: BriefClockDatabase? = null

        fun getInstance(context: Context): BriefClockDatabase {
            return instance ?: synchronized(this) {
                instance ?: BriefClockDatabase(context.applicationContext).also { instance = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_ALARMS (
                $COL_ALARM_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_ALARM_HOUR INTEGER NOT NULL,
                $COL_ALARM_MINUTE INTEGER NOT NULL,
                $COL_ALARM_LABEL TEXT,
                $COL_ALARM_ENABLED INTEGER NOT NULL DEFAULT 1,
                $COL_ALARM_DAYS INTEGER NOT NULL DEFAULT 0,
                $COL_ALARM_AUDIO_PATH TEXT,
                $COL_ALARM_SYSTEM INTEGER NOT NULL DEFAULT 0,
                $COL_ALARM_CREATED_AT INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_NAPS (
                $COL_NAP_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAP_TARGET INTEGER NOT NULL,
                $COL_NAP_ACTUAL INTEGER NOT NULL,
                $COL_NAP_SUCCESS INTEGER NOT NULL,
                $COL_NAP_TIMESTAMP INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Migration strategy if needed
    }

    // --- Alarm Operations ---

    @Synchronized
    fun insertAlarm(alarm: AlarmItem): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ALARM_HOUR, alarm.hour)
            put(COL_ALARM_MINUTE, alarm.minute)
            put(COL_ALARM_LABEL, alarm.label)
            put(COL_ALARM_ENABLED, if (alarm.isEnabled) 1 else 0)
            put(COL_ALARM_DAYS, alarm.daysOfWeek)
            put(COL_ALARM_AUDIO_PATH, alarm.audioPath)
            put(COL_ALARM_SYSTEM, if (alarm.isSystemAlarm) 1 else 0)
            put(COL_ALARM_CREATED_AT, alarm.createdAt)
        }
        return db.insert(TABLE_ALARMS, null, values)
    }

    @Synchronized
    fun updateAlarm(alarm: AlarmItem): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ALARM_HOUR, alarm.hour)
            put(COL_ALARM_MINUTE, alarm.minute)
            put(COL_ALARM_LABEL, alarm.label)
            put(COL_ALARM_ENABLED, if (alarm.isEnabled) 1 else 0)
            put(COL_ALARM_DAYS, alarm.daysOfWeek)
            put(COL_ALARM_AUDIO_PATH, alarm.audioPath)
            put(COL_ALARM_SYSTEM, if (alarm.isSystemAlarm) 1 else 0)
        }
        return db.update(TABLE_ALARMS, values, "$COL_ALARM_ID = ?", arrayOf(alarm.id.toString()))
    }

    @Synchronized
    fun setAlarmEnabled(id: Long, enabled: Boolean): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ALARM_ENABLED, if (enabled) 1 else 0)
        }
        return db.update(TABLE_ALARMS, values, "$COL_ALARM_ID = ?", arrayOf(id.toString()))
    }

    @Synchronized
    fun deleteAlarm(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_ALARMS, "$COL_ALARM_ID = ?", arrayOf(id.toString()))
    }

    @Synchronized
    fun getAllAlarms(): List<AlarmItem> {
        val db = readableDatabase
        val list = mutableListOf<AlarmItem>()
        val cursor: Cursor = db.query(
            TABLE_ALARMS,
            null,
            null,
            null,
            null,
            null,
            "$COL_ALARM_HOUR ASC, $COL_ALARM_MINUTE ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToAlarm(it))
            }
        }
        return list
    }

    @Synchronized
    fun getAlarmById(id: Long): AlarmItem? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_ALARMS,
            null,
            "$COL_ALARM_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )
        return cursor.use {
            if (it.moveToFirst()) cursorToAlarm(it) else null
        }
    }

    private fun cursorToAlarm(cursor: Cursor): AlarmItem {
        return AlarmItem(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ALARM_ID)),
            hour = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ALARM_HOUR)),
            minute = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ALARM_MINUTE)),
            label = cursor.getString(cursor.getColumnIndexOrThrow(COL_ALARM_LABEL)) ?: "",
            isEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ALARM_ENABLED)) == 1,
            daysOfWeek = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ALARM_DAYS)),
            audioPath = cursor.getString(cursor.getColumnIndexOrThrow(COL_ALARM_AUDIO_PATH)),
            isSystemAlarm = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ALARM_SYSTEM)) == 1,
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ALARM_CREATED_AT))
        )
    }

    // --- Nap Record Operations ---

    @Synchronized
    fun insertNapRecord(record: NapRecord): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_NAP_TARGET, record.targetDurationSec)
            put(COL_NAP_ACTUAL, record.actualDurationSec)
            put(COL_NAP_SUCCESS, if (record.isSuccess) 1 else 0)
            put(COL_NAP_TIMESTAMP, record.timestamp)
        }
        return db.insert(TABLE_NAPS, null, values)
    }

    @Synchronized
    fun getAllNapRecords(limit: Int = 100): List<NapRecord> {
        val db = readableDatabase
        val list = mutableListOf<NapRecord>()
        val cursor = db.query(
            TABLE_NAPS,
            null,
            null,
            null,
            null,
            null,
            "$COL_NAP_TIMESTAMP DESC",
            limit.toString()
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    NapRecord(
                        id = it.getLong(it.getColumnIndexOrThrow(COL_NAP_ID)),
                        targetDurationSec = it.getInt(it.getColumnIndexOrThrow(COL_NAP_TARGET)),
                        actualDurationSec = it.getInt(it.getColumnIndexOrThrow(COL_NAP_ACTUAL)),
                        isSuccess = it.getInt(it.getColumnIndexOrThrow(COL_NAP_SUCCESS)) == 1,
                        timestamp = it.getLong(it.getColumnIndexOrThrow(COL_NAP_TIMESTAMP))
                    )
                )
            }
        }
        return list
    }

    @Synchronized
    fun clearNapRecords(): Int {
        val db = writableDatabase
        return db.delete(TABLE_NAPS, null, null)
    }

    @Synchronized
    fun getNapStatistics(): NapStatistics {
        val allRecords = getAllNapRecords(limit = 1000)
        if (allRecords.isEmpty()) {
            return NapStatistics()
        }

        val totalGames = allRecords.size
        val successCount = allRecords.count { it.isSuccess }
        val failCount = totalGames - successCount
        val winRate = if (totalGames > 0) (successCount.toFloat() / totalGames * 100f) else 0f
        val totalDurationSec = allRecords.sumOf { it.actualDurationSec }
        val totalDurationMinutes = totalDurationSec / 60
        val avgDurationMinutes = if (totalGames > 0) (totalDurationSec / totalGames / 60) else 0

        // Calculate streaks (ordered chronologically from oldest to newest)
        val chronological = allRecords.sortedBy { it.timestamp }
        var currentStreak = 0
        var bestStreak = 0
        var runningStreak = 0
        for (rec in chronological) {
            if (rec.isSuccess) {
                runningStreak++
                if (runningStreak > bestStreak) {
                    bestStreak = runningStreak
                }
            } else {
                runningStreak = 0
            }
        }
        // Current streak from the most recent games backwards
        for (rec in allRecords) { // already desc by timestamp
            if (rec.isSuccess) {
                currentStreak++
            } else {
                break
            }
        }

        // Calculate last 7 days daily duration
        val recentDays = calculateRecent7Days(allRecords)

        return NapStatistics(
            totalGames = totalGames,
            successCount = successCount,
            failCount = failCount,
            winRatePercent = winRate,
            totalDurationMinutes = totalDurationMinutes,
            avgDurationMinutes = avgDurationMinutes,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            recentDays = recentDays,
            recentRecords = allRecords.take(15)
        )
    }

    private fun calculateRecent7Days(records: List<NapRecord>): List<DailyNapDuration> {
        val sdfKey = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val sdfDisplay = SimpleDateFormat("MM/dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Generate past 7 days (including today) in chronological order
        val dayList = mutableListOf<Calendar>()
        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            dayList.add(cal)
        }

        val recordsByDay = records.groupBy { sdfKey.format(Date(it.timestamp)) }

        return dayList.map { cal ->
            val key = sdfKey.format(cal.time)
            val display = sdfDisplay.format(cal.time)
            val dayRecords = recordsByDay[key] ?: emptyList()
            val totalSec = dayRecords.sumOf { it.actualDurationSec }
            DailyNapDuration(
                dateLabel = display,
                totalMinutes = (totalSec + 59) / 60, // round up to 1 min if non-zero
                successCount = dayRecords.count { it.isSuccess },
                failCount = dayRecords.count { !it.isSuccess }
            )
        }
    }
}
