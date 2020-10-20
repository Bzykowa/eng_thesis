package com.example.lockband.di

import android.content.Context
import com.example.lockband.data.AppDatabase
import com.example.lockband.data.AppStateDao
import com.example.lockband.data.ModeDao
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
    fun provideModeDao(appDatabase: AppDatabase) : ModeDao{
        return appDatabase.modeDao()
    }

}