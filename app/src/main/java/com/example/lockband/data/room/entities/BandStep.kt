package com.example.lockband.data.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Entity containing samples of user's steps from MiBand
 */
@Entity(tableName = "steps_band")
data class BandStep(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id : Int = 0,
    @ColumnInfo(name = "timestamp") val timestamp: Calendar,
    @ColumnInfo(name = "step_count") val stepCount: Int
)