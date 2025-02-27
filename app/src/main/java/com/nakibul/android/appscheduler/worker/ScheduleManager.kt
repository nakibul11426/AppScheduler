package com.nakibul.android.appscheduler.worker

import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.nakibul.android.appscheduler.models.AppSchedule
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleManager @Inject constructor(
    private val workManager: WorkManager
) {
    fun scheduleApp(schedule: AppSchedule) {
        val now = System.currentTimeMillis()
        val delay = schedule.scheduledTime - now

        if (delay <= 0) {
            Log.d("ScheduleManager", "Schedule ${schedule.id} is in the past, not scheduling")
            return
        }

        Log.d(
            "ScheduleManager", "Scheduling app ${schedule.appName} (${schedule.packageName}) " +
                    "with ID ${schedule.id} for ${schedule.scheduledTime} (delay: ${delay}ms)"
        )

        // Create input data for the worker
        val inputData = Data.Builder()
            .putLong(AppLaunchWorker.KEY_SCHEDULE_ID, schedule.id.toLong())
            .putString(AppLaunchWorker.KEY_PACKAGE_NAME, schedule.packageName)
            .putString(AppLaunchWorker.KEY_APP_NAME, schedule.appName)
            .build()

        // Create a unique work request for this schedule
        val workTag = "app_launch_${schedule.id}"
        val workRequest = OneTimeWorkRequestBuilder<AppLaunchWorker>()
            .setInputData(inputData)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(workTag)
            .build()

        // Enqueue as unique work to avoid duplicates
        workManager.enqueueUniqueWork(
            workTag,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

        Log.d("ScheduleManager", "Work scheduled with tag: $workTag")
    }

    fun cancelSchedule(scheduleId: Long) {
        val workTag = "app_launch_$scheduleId"
        Log.d("ScheduleManager", "Cancelling schedule: $scheduleId with tag: $workTag")
        workManager.cancelUniqueWork(workTag)
    }
}