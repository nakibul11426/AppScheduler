package com.nakibul.android.appscheduler.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nakibul.android.appscheduler.data.repository.ScheduleRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class RescheduleAlarmsWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val repository: ScheduleRepository,
    private val scheduleManager: ScheduleManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            Log.d("RescheduleWorker", "Starting to reschedule alarms")

            // Get all non-executed schedules
            val schedules = repository.allSchedules.first().filter { !it.isExecuted }

            if (schedules.isEmpty()) {
                Log.d("RescheduleWorker", "No active schedules to reschedule")
                return Result.success()
            }

            // Reschedule each one
            for (schedule in schedules) {
                if (schedule.scheduledTime > System.currentTimeMillis()) {
                    Log.d("RescheduleWorker", "Rescheduling: ${schedule.id} - ${schedule.appName}")
                    scheduleManager.scheduleApp(schedule)
                } else {
                    Log.d(
                        "RescheduleWorker",
                        "Schedule ${schedule.id} is in the past, marking as executed"
                    )
                    repository.markAsExecuted(schedule.id.toLong())
                }
            }

            Log.d("RescheduleWorker", "Successfully rescheduled ${schedules.size} alarms")
            return Result.success()
        } catch (e: Exception) {
            Log.e("RescheduleWorker", "Error rescheduling alarms", e)
            return Result.retry()
        }
    }
}