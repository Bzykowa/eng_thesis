package com.example.lockband.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_states")
data class AppState(
    @PrimaryKey @ColumnInfo(name = "package_name") val packageName : String,
    @ColumnInfo(name = "label") val label : String,
    @ColumnInfo(name = "lock_check", defaultValue = "0") val lockCheck : Boolean = false
){
    override fun toString(): String {
        return packageName
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppState

        if (packageName != other.packageName) return false
        if (label != other.label) return false
        if (lockCheck != other.lockCheck) return false

        return true
    }

    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + label.hashCode()
        result = 31 * result + lockCheck.hashCode()
        return result
    }
}