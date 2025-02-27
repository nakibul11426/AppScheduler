package com.nakibul.android.appscheduler.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.nakibul.android.appscheduler.worker.AppLaunchWorker
import com.nakibul.android.appscheduler.service.ForegroundAppLauncherService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var workManager: WorkManager

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Alarm triggered")

        val scheduleId = intent.getLongExtra("schedule_id", -1L)
        val packageName = intent.getStringExtra("package_name")
        val appName = intent.getStringExtra("app_name") ?: "App"

        if (scheduleId != -1L && packageName != null) {
            Log.d("AlarmReceiver", "Processing alarm for schedule: $scheduleId, app: $packageName")

            // Use WorkManager for Android 13+ due to background restrictions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                scheduleImmediateWork(scheduleId, packageName, appName)
            } else {
                // Use foreground service for lower Android versions
                ForegroundAppLauncherService.startService(context, scheduleId, packageName, appName)
            }
        } else {
            Log.e("AlarmReceiver", "Invalid schedule ID or package name in alarm intent")
        }
    }

    private fun scheduleImmediateWork(scheduleId: Long, packageName: String, appName: String) {
        val inputData = Data.Builder()
            .putLong(AppLaunchWorker.KEY_SCHEDULE_ID, scheduleId)
            .putString(AppLaunchWorker.KEY_PACKAGE_NAME, packageName)
            .putString(AppLaunchWorker.KEY_APP_NAME, appName)
            .build()

        val workTag = "immediate_app_launch_$scheduleId"
        val workRequest = OneTimeWorkRequestBuilder<AppLaunchWorker>()
            .setInputData(inputData)
            .addTag(workTag)
            .build()

        workManager.enqueueUniqueWork(
            workTag,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}