package com.uet.parking.data.model.enums

import com.google.firebase.firestore.PropertyName

enum class UserRole(val value: String) {
    ADMIN("admin"),
    GUARD("guard"),
    USER("user");

    companion object {
        fun fromString(value: String?) = values().find { it.value == value } ?: USER
    }
}

enum class TicketStatus(val value: String) {
    @PropertyName("Pending")
    PENDING("Pending"),

    @PropertyName("In Progress")
    IN_PROGRESS("In Progress"),

    @PropertyName("Done")
    DONE("Done"),

    @PropertyName("Confirmed")
    CONFIRMED("Confirmed");

    companion object {
        fun fromString(value: String?) = values().find { it.value == value } ?: PENDING
    }
}

enum class BookingStatus(val value: String) {
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    CHECKED_IN("Checked In");

    companion object {
        fun fromString(value: String?) = values().find { it.value == value } ?: PENDING
    }
}
