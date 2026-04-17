package com.uet.parking.data.model

data class DepositResponse(
    val status: String,
    val message: String,
    val newBalance: Int
)