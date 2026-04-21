package com.uet.parking.data.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true)
    val userId: Int = 0,
    val name: String?,
    val email: String,
    val password: String?,
    val debt: Double = 0.0
)
