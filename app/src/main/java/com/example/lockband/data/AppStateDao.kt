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

    //Deal with setting up lock on new app
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

}