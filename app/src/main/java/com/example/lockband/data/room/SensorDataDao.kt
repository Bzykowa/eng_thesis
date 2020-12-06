package com.example.lockband.data.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import java.util.*

@Dao
interface SensorDataDao {
    @Insert
    suspend fun insert(sensorDataSample : SensorData)

    @Delete
    suspend fun delete(sensorDataSample: SensorData)

    @Query("SELECT * FROM raw_sensor_data WHERE timestamp BETWEEN :startDate AND :endDate")
    fun getSamplesBetween(startDate : Calendar, endDate : Calendar) : List<SensorData>

    @Query("SELECT * FROM raw_sensor_data WHERE timestamp = (SELECT MAX(timestamp) FROM raw_sensor_data)")
    fun getLatestSample() : SensorData
}