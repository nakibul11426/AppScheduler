package com.nakibul.android.appscheduler.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nakibul.android.appscheduler.models.AppSchedule

@Database(entities = [AppSchedule::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao
}