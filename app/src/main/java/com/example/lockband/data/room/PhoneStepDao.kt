package com.example.lockband.data.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import java.util.*

@Dao
interface PhoneStepDao {

    @Insert
    suspend fun insert(stepSample : PhoneStep)

    @Delete
    suspend fun delete(stepSample: PhoneStep)

    @Query("SELECT * FROM steps_phone WHERE timestamp BETWEEN :startDate AND :endDate")
    fun getSamplesBetween(startDate : Calendar, endDate : Calendar) : List<PhoneStep>

}
