package com.example.lockband.di

import android.content.Context
import com.example.lockband.data.room.AppDatabase
import com.example.lockband.data.room.daos.AppStateDao
import com.example.lockband.data.room.daos.BandStepDao
import com.example.lockband.data.room.daos.HeartRateDao
import com.example.lockband.data.room.daos.PhoneStepDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideAppStateDao(appDatabase: AppDatabase): AppStateDao {
        return appDatabase.appStateDao()
    }

    @Provides
    fun provideBandStepDao(appDatabase: AppDatabase): BandStepDao {
        return appDatabase.bandStepDao()
    }

    @Provides
    fun providePhoneStepDao(appDatabase: AppDatabase): PhoneStepDao {
        return appDatabase.phoneStepDao()
    }

    @Provides
    fun provideHeartRateDao(appDatabase: AppDatabase): HeartRateDao {
        return appDatabase.heartRateDao()
    }

}