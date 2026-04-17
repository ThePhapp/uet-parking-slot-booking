package com.uet.parking.data.model

data class Payment(
    val id: String,
    val userId: String,
    val vehicleCode: String,
    val checkInTime: String,
    val checkOutTime: String,
    val amount: Int,
    val status: String,
    val createdAt: String
)