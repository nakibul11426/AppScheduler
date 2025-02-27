package com.nakibul.android.appscheduler.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.nakibul.android.appscheduler.worker.RescheduleAlarmsWorker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device rebooted, rescheduling all alarms")
            //reschedule alarms after boot
            val rescheduleWork = OneTimeWorkRequestBuilder<RescheduleAlarmsWorker>().build()
            WorkManager.getInstance(context).enqueue(rescheduleWork)
        }
    }
}