package com.uet.parking.data.model.enums

enum class UserRole(val value: String) {
    ADMIN("admin"),
    USER("user");

    companion object {
        fun fromString(value: String?) = values().find { it.value == value } ?: USER
    }
}

enum class TicketStatus(val value: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    DONE("Done"),
    CONFIRMED("Confirmed");

    companion object {
        fun fromString(value: String?) = values().find { it.value == value } ?: PENDING
    }
}
