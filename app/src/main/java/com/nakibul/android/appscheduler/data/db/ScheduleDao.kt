package com.nakibul.android.appscheduler.data.db
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nakibul.android.appscheduler.models.AppSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM app_schedules ORDER BY scheduledTime ASC")
    fun getAll(): Flow<List<AppSchedule>>

    @Query("SELECT * FROM app_schedules WHERE isExecuted = 0 ORDER BY scheduledTime ASC")
    fun getPendingSchedules(): Flow<List<AppSchedule>>

    @Query("SELECT * FROM app_schedules WHERE id = :scheduleId")
    suspend fun getScheduleById(scheduleId: Long): AppSchedule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: AppSchedule): Long

    @Update
    suspend fun update(schedule: AppSchedule)

    @Delete
    suspend fun delete(schedule: AppSchedule)

    @Query("UPDATE app_schedules SET isExecuted = 1 WHERE id = :scheduleId")
    suspend fun markAsExecuted(scheduleId: Long)
}