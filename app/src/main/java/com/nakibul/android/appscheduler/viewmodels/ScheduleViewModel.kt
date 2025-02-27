package com.nakibul.android.appscheduler.viewmodels

import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nakibul.android.appscheduler.worker.ScheduleManager
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
    private val scheduleManager: ScheduleManager,
    @ApplicationContext private val context: Context,
    private val alarmManager: AlarmManager
) : ViewModel() {
    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> get() = _installedApps

    init {
        Log.d("ScheduleViewModel", "Initializing")
        loadInstalledApps()
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
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                activity.startActivity(intent)
            }
        }
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            Log.d("ScheduleViewModel", "Loading installed apps")
            _installedApps.value = repository.getInstalledApps()
        }
    }

    // Get all schedules including executed ones
    val schedules = repository.allSchedules

    // Get only pending schedules
    val pendingSchedules = repository.getPendingSchedules()

    fun scheduleApp(packageName: String, appName: String, time: Long) {
        viewModelScope.launch {
            Log.d("ScheduleViewModel", "Scheduling app: $appName ($packageName) at $time")

            val schedule = AppSchedule(
                packageName = packageName,
                appName = appName,
                scheduledTime = time
            )

            // Insert into database
            val id = repository.insert(schedule).toInt()
            val finalSchedule = schedule.copy(id = id)

            // Schedule with WorkManager
            scheduleManager.scheduleApp(finalSchedule)

            Log.d("ScheduleViewModel", "App scheduled with ID: $id")
        }
    }

    fun cancelSchedule(schedule: AppSchedule) {
        viewModelScope.launch {
            Log.d("ScheduleViewModel", "Cancelling schedule: ${schedule.id}")
            repository.delete(schedule)
            scheduleManager.cancelSchedule(schedule.id.toLong())
        }
    }

    fun updateSchedule(schedule: AppSchedule) {
        viewModelScope.launch {
            Log.d("ScheduleViewModel", "Updating schedule: ${schedule.id}")
            repository.update(schedule)
            scheduleManager.cancelSchedule(schedule.id.toLong())
            scheduleManager.scheduleApp(schedule)
        }
    }
}