package com.example.lockband.data

import androidx.annotation.WorkerThread
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStateRepository @Inject constructor(private val appStateDao: AppStateDao) {

    fun getAppState(appName: String) = appStateDao.getAppState(appName)

    fun getAppStates() = appStateDao.getAppStates()

    fun getLockedApps() = appStateDao.getLockedApps()

    fun isLocked(appName: String) = appStateDao.isLocked(appName)

    @WorkerThread
    suspend fun upsertAppState(app: AppState) = appStateDao.upsert(app)

    @WorkerThread
    suspend fun insertAppState(app : AppState) = appStateDao.insert(app)

    @WorkerThread
    suspend fun updateAppState(app: AppState) = appStateDao.update(app)
}