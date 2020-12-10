package com.example.lockband.data.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "steps_band")
data class BandStep(
    @PrimaryKey @ColumnInfo(name = "timestamp") val timestamp: Calendar,
    @ColumnInfo(name = "step_count") val stepCount: Int
) {
}