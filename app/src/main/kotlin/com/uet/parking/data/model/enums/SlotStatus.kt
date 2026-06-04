package com.uet.parking.data.model.enums

import com.google.firebase.firestore.PropertyName

enum class SlotStatus(val value: String) {
    @PropertyName("Available")
    AVAILABLE("Available"),

    @PropertyName("Occupied")
    OCCUPIED("Occupied");

    companion object {
        fun fromString(value: String?) = values().find { it.value == value } ?: AVAILABLE
    }
}
