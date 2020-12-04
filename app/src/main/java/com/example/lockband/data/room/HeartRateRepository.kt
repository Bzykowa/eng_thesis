package com.example.lockband.data.room

import androidx.annotation.WorkerThread
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeartRateRepository @Inject constructor(private val heartRateDao: HeartRateDao) {

    @WorkerThread
    suspend fun insertHeartRateSample(hrSample: HeartRate) = heartRateDao.insert(hrSample)

    @WorkerThread
    suspend fun deleteHeartRateSample(hrSample: HeartRate) = heartRateDao.delete(hrSample)

    fun getHeartRateSamplesBetween(startDate: Calendar, endDate: Calendar) =
        heartRateDao.getSamplesBetween(startDate, endDate)
}