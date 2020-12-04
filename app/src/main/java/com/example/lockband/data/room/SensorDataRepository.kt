package com.example.lockband.data.room

import androidx.annotation.CallSuper
import androidx.annotation.WorkerThread
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
}