package com.example.lockband.data.room.repos

import androidx.annotation.WorkerThread
import com.example.lockband.data.room.daos.BandStepDao
import com.example.lockband.data.room.daos.PhoneStepDao
import com.example.lockband.data.room.entities.BandStep
import com.example.lockband.data.room.entities.PhoneStep
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

    fun getLatestPhoneStepSampleLive() = phoneStepDao.getLatestSampleLive()

    fun getLatestPhoneStepSample() = phoneStepDao.getLatestSample()

    @WorkerThread
    suspend fun insertBandStepSample(stepSample: BandStep) = bandStepDao.insert(stepSample)

    @WorkerThread
    suspend fun deleteBandStepSample(stepSample: BandStep) = bandStepDao.delete(stepSample)

    fun getBandStepSamplesBetween(startDate: Calendar, endDate: Calendar) =
        bandStepDao.getSamplesBetween(startDate, endDate)

    fun getLatestBandStepSampleLive() = bandStepDao.getLatestSampleLive()

    fun getLatestBandStepSample() = bandStepDao.getLatestSample()
}