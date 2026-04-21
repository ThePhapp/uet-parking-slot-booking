package com.uet.parking.data.model.payment

data class DepositRequest(
    val userId: String,
    val amount: Int
)