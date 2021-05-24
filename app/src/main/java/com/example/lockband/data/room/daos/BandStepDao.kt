package com.example.lockband.data.room.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.lockband.data.room.entities.BandStep
import java.util.*

@Dao
interface BandStepDao {

    @Insert
    suspend fun insert(stepSample : BandStep)

    @Delete
    suspend fun delete(stepSample: BandStep)

    @Query("SELECT * FROM steps_band WHERE timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp")
    fun getSamplesBetween(startDate : Calendar, endDate : Calendar) : List<BandStep>

    @Query("SELECT * FROM steps_band WHERE timestamp = (SELECT MAX(timestamp) FROM steps_band)")
    fun getLatestSampleLive() : LiveData<BandStep>

    @Query("SELECT * FROM steps_band WHERE timestamp = (SELECT MAX(timestamp) FROM steps_band)")
    fun getLatestSample() : BandStep
}
