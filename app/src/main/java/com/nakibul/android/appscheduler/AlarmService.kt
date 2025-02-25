package com.nakibul.android.appscheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.nakibul.android.appscheduler.data.repository.ScheduleRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmService : Service() {

    @Inject
    lateinit var repository: ScheduleRepository

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("AlarmService", "Service started")
        val scheduleId = intent?.getLongExtra("schedule_id", -1L) ?: -1L
        val packageName = intent?.getStringExtra("package_name")

        if (scheduleId != -1L && packageName != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val isExecuted = repository.isScheduleExecuted(scheduleId)
                if (!isExecuted) {
                    // Launch the scheduled app
                    val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                    launchIntent?.let {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(it)
                        Log.d("AlarmService", "App launched: $packageName")
                    }

                    // Mark the schedule as executed in the database
                    repository.markAsExecuted(scheduleId)
                    Log.d("AlarmService", "Schedule marked as executed: $scheduleId")

                    // Cancel the alarm
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val alarmIntent = Intent(this@AlarmService, AlarmReceiver::class.java).apply {
                        putExtra("schedule_id", scheduleId)
                        putExtra("package_name", packageName)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        this@AlarmService,
                        scheduleId.toInt(),
                        alarmIntent,
                        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                    )
                    pendingIntent?.let {
                        alarmManager.cancel(it)
                        it.cancel()
                        Log.d("AlarmService", "Alarm canceled: $scheduleId")
                    }
                }
                stopSelf(startId)
            }
        } else {
            stopSelf(startId)
        }

        return START_NOT_STICKY
    }
}