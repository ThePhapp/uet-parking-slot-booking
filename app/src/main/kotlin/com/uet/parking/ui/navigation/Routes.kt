package com.uet.parking.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable
    data object Auth : Route

    @Serializable
    data object Home : Route

    @Serializable
    data object Booking : Route

    @Serializable
    data object Searching : Route

    @Serializable
    data object Success : Route

    @Serializable
    data object Tickets : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object AdminHome : Route

    @Serializable
    data class AdminDetail(val lotId: Int) : Route

    @Serializable
    data object AdminBooking : Route

    @Serializable
    data object AdminSettings : Route
}
