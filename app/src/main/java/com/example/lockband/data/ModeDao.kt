package com.example.lockband.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ModeDao {
    @Insert
    suspend fun insertAll(modes: List<Mode>)

    @Delete
    suspend fun delete(mode: Mode)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(mode: Mode): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(mode: Mode)

    @Query("SELECT * FROM modes")
    fun getModes() : LiveData<List<Mode>>

    @Query("SELECT * FROM modes WHERE name = :name")
    fun getMode(name : String) : LiveData<Mode>
}