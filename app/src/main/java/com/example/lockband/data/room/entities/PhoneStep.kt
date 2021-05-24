package com.example.lockband.data.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Entity containing user's step samples measured by phone's step counter sensor
 */
@Entity(tableName = "steps_phone")
data class PhoneStep(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id : Int = 0,
    @ColumnInfo(name = "timestamp") val timestamp: Calendar,
    @ColumnInfo(name = "step_count") val stepCount: Int,
    @ColumnInfo(name = "offset") val offset: Int
)