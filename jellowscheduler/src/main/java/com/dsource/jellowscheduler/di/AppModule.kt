package com.dsource.jellowscheduler.di

import android.content.Context
import androidx.room.Room
import com.dsource.jellowscheduler.datasource.localdb.dao.SchedulerDao
import com.dsource.jellowscheduler.datasource.localdb.database.SchedulerDb
import com.dsource.jellowscheduler.repositories.ISchedulerRepository
import com.dsource.jellowscheduler.repositories.SchedulerRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideSchedulerDb(@ApplicationContext context: Context): SchedulerDb {
        return Room.databaseBuilder(
            context.applicationContext,
            SchedulerDb::class.java,
            "scheduler_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideSchedulerRepository(schedulerDao: SchedulerDao): ISchedulerRepository {
        return SchedulerRepositoryImpl(schedulerDao)
    }

}