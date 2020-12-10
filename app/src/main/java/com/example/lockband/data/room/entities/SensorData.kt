package com.example.lockband.data.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "raw_sensor_data")
data class SensorData(
    @PrimaryKey @ColumnInfo(name = "timestamp") val timestamp: Calendar,
    @ColumnInfo(name = "x_axis") val xAxis: Int,
    @ColumnInfo(name = "y_axis") val yAxis: Int,
    @ColumnInfo(name = "z_axis") val zAxis: Int
) {
}