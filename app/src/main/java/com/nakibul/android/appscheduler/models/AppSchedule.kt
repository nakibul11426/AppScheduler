package com.nakibul.android.appscheduler.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class AppSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val appName: String,
    val scheduledTime: Long,
    var isActive: Boolean = true,
    var isExecuted: Boolean = false
)
