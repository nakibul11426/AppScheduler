package com.nakibul.android.appscheduler.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_schedules")
data class AppSchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val packageName: String,
    val appName: String,
    val scheduledTime: Long,
    val isExecuted: Boolean = false,
    val isRepeating: Boolean = false,
    val repeatInterval: Long = 0 // For potential future enhancement
)
