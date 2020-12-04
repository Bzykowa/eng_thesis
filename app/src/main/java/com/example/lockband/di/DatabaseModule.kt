package com.example.lockband.di

import android.content.Context
import com.example.lockband.data.room.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
class DatabaseModule {
    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context : Context) : AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideAppStateDao(appDatabase: AppDatabase) : AppStateDao {
        return appDatabase.appStateDao()
    }

    @Provides
    fun provideBandStepDao(appDatabase: AppDatabase) : BandStepDao {
        return appDatabase.bandStepDao()
    }

    @Provides
    fun providePhoneStepDao(appDatabase: AppDatabase) : PhoneStepDao {
        return appDatabase.phoneStepDao()
    }

    @Provides
    fun provideHeartRateDao(appDatabase: AppDatabase) : HeartRateDao {
        return appDatabase.heartRateDao()
    }

    @Provides
    fun provideSensorDataDao(appDatabase: AppDatabase) : SensorDataDao {
        return appDatabase.sensorDataDao()
    }

}