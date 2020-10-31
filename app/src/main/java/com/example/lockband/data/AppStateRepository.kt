package com.example.lockband.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import javax.inject.Inject
import javax.inject.Singleton
import androidx.annotation.WorkerThread

@Singleton
class AppStateRepository @Inject constructor(private val appStateDao: AppStateDao) {

    fun getAppState(appName: String) = appStateDao.getAppState(appName)

    fun getAppStates() = appStateDao.getAppStates()

    @WorkerThread
    suspend fun upsertAppState(app: AppState) = appStateDao.upsert(app)

    @WorkerThread
    suspend fun insertAppState(app : AppState) = appStateDao.insert(app)

    @WorkerThread
    suspend fun updateAppState(app: AppState) = appStateDao.update(app)
}