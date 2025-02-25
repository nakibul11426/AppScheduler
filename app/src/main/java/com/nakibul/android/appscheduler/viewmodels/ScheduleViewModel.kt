package com.nakibul.android.appscheduler.viewmodels

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nakibul.android.appscheduler.AlarmReceiver
import com.nakibul.android.appscheduler.data.repository.ScheduleRepository
import com.nakibul.android.appscheduler.models.AppInfo
import com.nakibul.android.appscheduler.models.AppSchedule
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: ScheduleRepository,
    @ApplicationContext private val context: Context,
    private val alarmManager: AlarmManager
) : ViewModel() {
    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> get() = _installedApps

    init {
        loadInstalledApps()
        rescheduleAllAlarms()
    }

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            // Below Android 12, no special permission is required
            true
        }
    }

    fun requestExactAlarmPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                activity.startActivity(intent)
            }
        }
    }

    private fun rescheduleAllAlarms() {
        viewModelScope.launch {
            repository.allSchedules.collect { schedules ->
                schedules.forEach { setAlarm(it) }
            }
        }
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            _installedApps.value = repository.getInstalledApps()
        }
    }

    val schedules = repository.allSchedules

    fun scheduleApp(packageName: String, appName: String, time: Long) {
        viewModelScope.launch {
            val tempSchedule = AppSchedule(
                packageName = packageName,
                appName = appName,
                scheduledTime = time
            )
            val scheduleId = repository.insert(tempSchedule).toInt()
            val schedule = tempSchedule.copy(id = scheduleId)
            setAlarm(schedule)
            Log.d("ScheduleViewModel", "Scheduled app: ${schedule.id}")
        }
    }


    fun cancelSchedule(schedule: AppSchedule) {
        viewModelScope.launch {
            repository.delete(schedule)
            cancelAlarm(schedule)
        }
    }


    fun updateSchedule(schedule: AppSchedule) {
        viewModelScope.launch {
            repository.update(schedule)
            cancelAlarm(schedule)
            setAlarm(schedule)
        }
    }


    private fun setAlarm(schedule: AppSchedule) {
        if (!canScheduleExactAlarms()) {
            Log.e("ScheduleViewModel", "Cannot set exact alarm: Permission denied")
            return
        }

        // Create an intent for the BroadcastReceiver
        val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("schedule_id", schedule.id)
            putExtra("package_name", schedule.packageName)
        }

        // Create a unique PendingIntent for the BroadcastReceiver
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id.toInt(), // Use a unique request code (e.g., schedule ID)
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set the exact alarm
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            schedule.scheduledTime,
            pendingIntent
        )
        Log.d("ScheduleViewModel", "Alarm set for schedule: ${schedule.id}")
    }

    private fun cancelAlarm(schedule: AppSchedule) {
        val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("schedule_id", schedule.id)
            putExtra("package_name", schedule.packageName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id.toInt(),
            alarmIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
            Log.d("ScheduleViewModel", "Alarm canceled: ${schedule.id}")
        }
    }

    fun cancelScheduleById(scheduleId: Long) {
        viewModelScope.launch {
            val schedule = repository.getScheduleById(scheduleId)
            schedule?.let {
                repository.delete(it)
                cancelAlarm(it)
            }
        }
    }

    fun markAsExecuted(scheduleId: Long) {
        viewModelScope.launch {
            repository.markAsExecuted(scheduleId)
        }
    }
}