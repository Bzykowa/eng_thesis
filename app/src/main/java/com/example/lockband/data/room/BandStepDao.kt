package com.example.lockband.data.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import java.util.*

@Dao
interface BandStepDao {

    @Insert
    suspend fun insert(stepSample : BandStep)

    @Delete
    suspend fun delete(stepSample: BandStep)

    @Query("SELECT * FROM steps_band WHERE timestamp BETWEEN :startDate AND :endDate")
    fun getSamplesBetween(startDate : Calendar, endDate : Calendar) : List<BandStep>

}
