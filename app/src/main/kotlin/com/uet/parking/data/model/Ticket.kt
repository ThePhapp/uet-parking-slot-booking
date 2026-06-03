package com.uet.parking.data.model

import com.uet.parking.data.model.enums.TicketStatus

data class Ticket(
    val ticketId: String? = null,
    val userId: String? = null,
    val parkingId: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val status: TicketStatus? = TicketStatus.PENDING,
    val price: Double? = 0.0,
    val assignedSlotId: String? = null,
    val checkedInAt: Long? = null
) {
    constructor() : this(null, null, null, null, null, TicketStatus.PENDING, 0.0, null, null)
}
