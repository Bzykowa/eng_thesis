package com.example.lockband.data.room.repos

import androidx.annotation.WorkerThread
import com.example.lockband.data.room.daos.HeartRateDao
import com.example.lockband.data.room.entities.HeartRate
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

    fun getLatestHeartRateSample() = heartRateDao.getLatestSample()
}