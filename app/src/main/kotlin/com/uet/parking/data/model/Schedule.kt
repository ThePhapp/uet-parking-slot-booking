package com.uet.parking.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class Schedule (
    @PrimaryKey
    val id: Int
)
