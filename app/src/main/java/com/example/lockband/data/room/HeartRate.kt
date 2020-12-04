package com.example.lockband.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "heart_rate")
data class HeartRate(
    @PrimaryKey @ColumnInfo(name = "timestamp") val timestamp: Calendar,
    @ColumnInfo(name = "heart_rate") val heartRate : Int
) {
}