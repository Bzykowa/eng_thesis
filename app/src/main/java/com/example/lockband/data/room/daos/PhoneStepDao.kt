package com.example.lockband.data.room.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.lockband.data.room.entities.PhoneStep
import java.util.*

@Dao
interface PhoneStepDao {

    @Insert
    suspend fun insert(stepSample : PhoneStep)

    @Delete
    suspend fun delete(stepSample: PhoneStep)

    @Query("SELECT * FROM steps_phone WHERE timestamp BETWEEN :startDate AND :endDate")
    fun getSamplesBetween(startDate : Calendar, endDate : Calendar) : List<PhoneStep>

    @Query("SELECT * FROM steps_phone WHERE timestamp = (SELECT MAX(timestamp) FROM steps_phone)")
    fun getLatestSampleLive() : LiveData<PhoneStep>

    @Query("SELECT * FROM steps_phone WHERE timestamp = (SELECT MAX(timestamp) FROM steps_phone)")
    fun getLatestSample() : PhoneStep

}
