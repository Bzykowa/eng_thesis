package com.example.lockband.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "modes")
data class Mode(
    @PrimaryKey @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "state") val state: Boolean
) {
    override fun toString(): String {
        return name
    }
}