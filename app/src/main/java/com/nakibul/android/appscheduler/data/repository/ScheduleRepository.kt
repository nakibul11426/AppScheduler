package com.nakibul.android.appscheduler.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import com.nakibul.android.appscheduler.data.db.ScheduleDao
import com.nakibul.android.appscheduler.models.AppInfo
import com.nakibul.android.appscheduler.models.AppSchedule
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class ScheduleRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: ScheduleDao
) {
    val allSchedules = dao.getAll()

    suspend fun insert(schedule: AppSchedule): Long = dao.insert(schedule)
    suspend fun update(schedule: AppSchedule) = dao.update(schedule)
    suspend fun delete(schedule: AppSchedule) = dao.delete(schedule)
    suspend fun getScheduleById(scheduleId: Long): AppSchedule? {
        return dao.getScheduleById(scheduleId)
    }
    suspend fun markAsExecuted(scheduleId: Long) {
        dao.markAsExecuted(scheduleId)
    }
    suspend fun isScheduleExecuted(scheduleId: Long): Boolean {
        val schedule = dao.getScheduleById(scheduleId)
        return schedule?.isExecuted ?: false
    }
    /* get all the installed app including system installed app*/
    fun getAllInstalledApps(): List<AppInfo> {
        val packageManager = context.packageManager
        val installedApps = mutableListOf<AppInfo>()

        val packages = packageManager.getInstalledPackages(0)
        for (packageInfo in packages) {
            val appName = packageInfo.applicationInfo?.loadLabel(packageManager).toString()
            val icon = packageInfo.applicationInfo?.loadIcon(packageManager)
            icon?.let {
                AppInfo(
                    packageName = packageInfo.packageName,
                    appName = appName,
                    icon = it
                )
            }?.let {
                installedApps.add(
                    it
                )
            }
        }

        return installedApps
    }

    /*get all the installed app excluding system installed app*/
    fun getInstalledApps(): List<AppInfo> {
        val packageManager = context.packageManager
        val installedApps = mutableListOf<AppInfo>()

        val packages = packageManager.getInstalledPackages(0)
        for (packageInfo in packages) {
            if (packageInfo.applicationInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM) == 0) {
                val appName = packageInfo.applicationInfo?.loadLabel(packageManager).toString()
                val icon = packageInfo.applicationInfo?.loadIcon(packageManager)
                icon?.let {
                    AppInfo(
                        packageName = packageInfo.packageName,
                        appName = appName,
                        icon = it
                    )
                }?.let {
                    installedApps.add(it)
                }
            }
        }
        return installedApps
    }
}