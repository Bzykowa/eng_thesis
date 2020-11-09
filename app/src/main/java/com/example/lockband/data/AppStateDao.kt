package com.example.lockband.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AppStateDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(apps: List<AppState>)

    @Delete
    suspend fun delete(app: AppState)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(app: AppState): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(app: AppState)

    @Transaction
    suspend fun upsert(app: AppState) {
        val id = insert(app)
        if (id == -1L) {
            update(app)
        }
    }

    @Query("SELECT * FROM app_states ORDER BY label ASC")
    fun getAppStates(): LiveData<List<AppState>>


    @Query("SELECT * FROM app_states WHERE package_name = :appName")
    fun getAppState(appName : String): LiveData<AppState>

    @Query("SELECT lock_check from app_states WHERE package_name = :appName")
    fun isLocked(appName: String) : Boolean

    @Query("SELECT package_name from app_states WHERE lock_check == 1")
    fun getLockedApps() : List<String>

}