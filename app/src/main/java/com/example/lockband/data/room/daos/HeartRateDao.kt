package com.example.lockband.data.room.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.lockband.data.room.entities.HeartRate
import java.util.*


@Dao
interface HeartRateDao {

    @Insert
    suspend fun insert(heartRateSample : HeartRate)

    @Delete
    suspend fun delete(heartRateSample: HeartRate)

    @Query("SELECT * FROM heart_rate WHERE timestamp BETWEEN :startDate AND :endDate")
    fun getSamplesBetween(startDate : Calendar, endDate : Calendar) : List<HeartRate>

    @Query("SELECT * FROM heart_rate WHERE timestamp = (SELECT MAX(timestamp) FROM heart_rate)")
    fun getLatestSample() : LiveData<HeartRate>
}