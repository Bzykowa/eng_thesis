package com.example.lockband.data.room

import androidx.annotation.WorkerThread
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepRepository @Inject constructor(private val bandStepDao: BandStepDao, private val phoneStepDao : PhoneStepDao) {

    @WorkerThread
    suspend fun insertPhoneStepSample(stepSample: PhoneStep) = phoneStepDao.insert(stepSample)

    @WorkerThread
    suspend fun deletePhoneStepSample(stepSample: PhoneStep) = phoneStepDao.delete(stepSample)

    fun getPhoneStepSamplesBetween(startDate: Calendar, endDate: Calendar) =
        phoneStepDao.getSamplesBetween(startDate, endDate)

    fun getLatestPhoneStepSample() = phoneStepDao.getLatestSample()

    @WorkerThread
    suspend fun insertBandStepSample(stepSample: BandStep) = bandStepDao.insert(stepSample)

    @WorkerThread
    suspend fun deleteBandStepSample(stepSample: BandStep) = bandStepDao.delete(stepSample)

    fun getBandStepSamplesBetween(startDate: Calendar, endDate: Calendar) =
        bandStepDao.getSamplesBetween(startDate, endDate)

    fun getLatestBandStepSample() = bandStepDao.getLatestSample()
}