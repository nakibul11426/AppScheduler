package com.nakibul.android.appscheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nakibul.android.appscheduler.data.db.AppDatabase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

class ScheduleReceiver : BroadcastReceiver() {
    @Inject
    lateinit var database: AppDatabase

    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.getStringExtra("packageName") ?: return
        val scheduleId = intent.getIntExtra("scheduleId", -1)

        // Initialize Hilt
        EntryPointAccessors.fromApplication(
            context,
            ScheduleReceiverEntryPoint::class.java
        ).also {
            inject(context)
        }

        // ... rest of the receiver code
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ScheduleReceiverEntryPoint {
        fun database(): AppDatabase
    }

    private fun inject(appContext: Context) {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            appContext,
            ScheduleReceiverEntryPoint::class.java
        )
        database = hiltEntryPoint.database()
    }
}