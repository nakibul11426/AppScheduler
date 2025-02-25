package com.nakibul.android.appscheduler.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nakibul.android.appscheduler.models.AppSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules")
    fun getAll(): Flow<List<AppSchedule>>

    @Insert
    suspend fun insert(schedule: AppSchedule): Long

    @Update
    suspend fun update(schedule: AppSchedule)

    @Delete
    suspend fun delete(schedule: AppSchedule)

    @Query("SELECT * FROM schedules WHERE id = :scheduleId")
    suspend fun getScheduleById(scheduleId: Long): AppSchedule?

    @Query("UPDATE schedules SET isExecuted = 1 WHERE id = :scheduleId")
    suspend fun markAsExecuted(scheduleId: Long)
}