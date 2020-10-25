package com.example.lockband.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStateRepository @Inject constructor(private val appStateDao: AppStateDao) {

    fun getAppState(appName: String) = appStateDao.getAppState(appName)

    fun getAppStates() = appStateDao.getAppStates()

    fun upsertAppState(app : AppState) = appStateDao.upsert(app)
}