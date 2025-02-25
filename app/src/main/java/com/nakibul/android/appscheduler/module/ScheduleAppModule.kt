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
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_schedule.db"
        ).build()
    }

    @Provides
    fun provideScheduleDao(database: AppDatabase): ScheduleDao {
        return database.scheduleDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun provideScheduleRepository(
        @ApplicationContext context: Context, // Provide Context
        dao: ScheduleDao // Provide ScheduleDao
    ): ScheduleRepository {
        return ScheduleRepository(context, dao) // Pass both parameters
    }
}

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @Provides
    fun provideAlarmManager(@ApplicationContext context: Context): AlarmManager {
        return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }
}
