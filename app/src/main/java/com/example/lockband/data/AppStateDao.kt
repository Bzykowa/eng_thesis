package com.example.lockband.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AppStateDao {

    @Insert
    fun insertAll(apps: List<AppState>)

    @Delete
    fun delete(app: AppState)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(app: AppState): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(app: AppState)

    //Deal with setting up lock on new app
    @Transaction
    fun upsert(app: AppState) {
        val id = insert(app)
        if (id == -1L) {
            update(app)
        }
    }

    @Query("SELECT * FROM app_states")
    fun getAppStates(): LiveData<List<AppState>>


    @Query("SELECT * FROM app_states WHERE package_name = :appName")
    fun getAppState(appName : String): LiveData<AppState>

}