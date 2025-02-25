package com.nakibul.android.appscheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Alarm triggered")
        val scheduleId = intent.getIntExtra("schedule_id", -1).toLong()
        val packageName = intent.getStringExtra("package_name")

        if (scheduleId != -1L && packageName != null) {
            // Start the AlarmService to handle the alarm trigger
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                putExtra("schedule_id", scheduleId)
                putExtra("package_name", packageName)
            }


            context.startService(serviceIntent)

        }
    }
}