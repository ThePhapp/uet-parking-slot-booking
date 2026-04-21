package com.uet.parking.data.model

import androidx.room3.Entity

@Entity(tableName = "schedules")
data class Schedule (
    val id: String
)
