package com.example.lockband.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStateRepository @Inject constructor(private val appStateDao: AppStateDao) {

    fun getAppState(appName: String) = appStateDao.getAppState(appName)

    fun getAppStates() = appStateDao.getAppStates()

    fun upsertAppState(app : AppState) = appStateDao.upsert(app)
}