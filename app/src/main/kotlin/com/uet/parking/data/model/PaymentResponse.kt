package com.uet.parking.data.model

data class PaymentResponse(
    val status: String,
    val transactionId: String,
    val message: String
)