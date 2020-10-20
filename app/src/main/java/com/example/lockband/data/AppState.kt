package com.example.lockband.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_states")
data class AppState(
    @PrimaryKey @ColumnInfo(name = "package_name") val packageName : String,
    @ColumnInfo(name = "lock_check", defaultValue = "0") val lockCheck : Boolean = false
){
    override fun toString(): String {
        return packageName
    }
}