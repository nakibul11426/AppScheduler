package com.nakibul.android.appscheduler.module

import android.app.AlarmManager
import android.content.Context
import androidx.room.Room
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
    fun provideScheduleDao(database: AppDatabase): ScheduleDao = database.scheduleDao()

    @Provides
    fun provideScheduleRepository(dao: ScheduleDao,@ApplicationContext context: Context): ScheduleRepository =
        ScheduleRepository(context,dao)

    @Provides
    fun provideAlarmManager(@ApplicationContext context: Context): AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
}