package com.nakibul.android.appscheduler.module

import android.app.AlarmManager
import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.nakibul.android.appscheduler.data.db.AppDatabase
import com.nakibul.android.appscheduler.data.db.ScheduleDao
import com.nakibul.android.appscheduler.data.repository.ScheduleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "app_schedule.db").build()

    @Provides
    @Singleton
    fun provideScheduleDao(database: AppDatabase): ScheduleDao = database.scheduleDao()

    @Provides
    @Singleton
    fun provideScheduleRepository(
        dao: ScheduleDao,
        @ApplicationContext context: Context
    ): ScheduleRepository =
        ScheduleRepository(context, dao)

    @Provides
    @Singleton
    fun provideAlarmManager(@ApplicationContext context: Context): AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}