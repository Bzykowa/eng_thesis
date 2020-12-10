package com.example.lockband.data.room.repos

import androidx.annotation.WorkerThread
import com.example.lockband.data.room.daos.SensorDataDao
import com.example.lockband.data.room.entities.SensorData
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorDataRepository @Inject constructor(private val sensorDataDao: SensorDataDao) {

    @WorkerThread
    suspend fun insertSensorDataSample(sensorDataSample: SensorData) =
        sensorDataDao.insert(sensorDataSample)

    @WorkerThread
    suspend fun deleteSensorDataSample(sensorDataSample: SensorData) =
        sensorDataDao.delete(sensorDataSample)

    fun getSensorDataSamplesBetween(startDate: Calendar, endDate: Calendar) =
        sensorDataDao.getSamplesBetween(startDate, endDate)

    fun getLatestSensorDataSample() = sensorDataDao.getLatestSample()
}