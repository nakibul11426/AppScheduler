package com.nakibul.android.appscheduler.worker

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.nakibul.android.appscheduler.R
import com.nakibul.android.appscheduler.data.repository.ScheduleRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class AppLaunchWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val repository: ScheduleRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val NOTIFICATION_ID = 123
        const val KEY_SCHEDULE_ID = "schedule_id"
        const val KEY_PACKAGE_NAME = "package_name"
        const val KEY_APP_NAME = "app_name"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val scheduleId = workerParams.inputData.getLong(KEY_SCHEDULE_ID, -1L)
            val packageName = workerParams.inputData.getString(KEY_PACKAGE_NAME)
                ?: return@withContext Result.failure()
            val appName = workerParams.inputData.getString(KEY_APP_NAME) ?: "App"

            Log.d(
                "AppLaunchWorker",
                "Starting work for schedule: $scheduleId, package: $packageName"
            )

            if (scheduleId != -1L) {
                // Check if this schedule has already been executed
                val isExecuted = repository.isScheduleExecuted(scheduleId)
                if (!isExecuted) {
                    // Launch the app
                    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
                    if (intent != null) {
                        Log.d("AppLaunchWorker", "Launching app: $packageName")
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                        context.startActivity(intent)
                        // Mark as executed
                        repository.markAsExecuted(scheduleId)
                        Log.d("AppLaunchWorker", "Marked schedule $scheduleId as executed")

                        return@withContext Result.success()
                    } else {
                        Log.e(
                            "AppLaunchWorker",
                            "Failed to get launch intent for package: $packageName"
                        )
                    }
                } else {
                    Log.d("AppLaunchWorker", "Schedule $scheduleId already executed")
                }
            } else {
                Log.e("AppLaunchWorker", "Invalid schedule ID")
            }

            Result.failure()
        } catch (e: Exception) {
            Log.e("AppLaunchWorker", "Error launching app", e)
            Result.failure()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val packageName = workerParams.inputData.getString(KEY_PACKAGE_NAME) ?: "Unknown app"
        val appName = workerParams.inputData.getString(KEY_APP_NAME) ?: "App"
        val title = "Launching scheduled app"
        val notification = NotificationCompat.Builder(context, "app_launcher_channel")
            .setContentTitle(title)
            .setContentText("Launching $appName")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setAutoCancel(true) // Ensure the notification is dismissed when the task is done
            .build()

        return ForegroundInfo(NOTIFICATION_ID, notification)
    }
}