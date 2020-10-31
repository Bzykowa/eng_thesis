package com.example.lockband.data

import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ModeRepository @Inject constructor(private val modeDao: ModeDao) {
    fun getModes() = modeDao.getModes()
    fun getMode(name: String) = modeDao.getMode(name)
    suspend fun updateMode(mode: Mode) = modeDao.update(mode)
}