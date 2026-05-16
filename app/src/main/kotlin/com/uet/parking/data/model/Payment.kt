package com.uet.parking.data.model

data class Payment(
    val paymentId: String? = null,
    val userId: String? = null,
    val amount: Double? = null,
    val status: String? = null,
    val createdAt: String? = null
) {
    constructor() : this(null, null, null, null, null)
}
