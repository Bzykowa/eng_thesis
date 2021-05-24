package com.example.lockband.data.room.repos

import androidx.annotation.WorkerThread
import com.example.lockband.data.room.daos.AppStateDao
import com.example.lockband.data.room.entities.AppState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStateRepository @Inject constructor(private val appStateDao: AppStateDao) {

    fun getAppStates() = appStateDao.getAppStates()

    fun getLockedApps() = appStateDao.getLockedApps()

    @WorkerThread
    suspend fun upsertAppState(app: AppState) = appStateDao.upsert(app)

    @WorkerThread
    suspend fun insertAppState(app : AppState) = appStateDao.insert(app)

    @WorkerThread
    suspend fun updateAppState(app: AppState) = appStateDao.update(app)
}