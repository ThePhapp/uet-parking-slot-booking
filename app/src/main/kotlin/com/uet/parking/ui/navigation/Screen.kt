package com.uet.parking.ui.navigation

enum class Screen(val route: String) {
    AUTH("auth"),
    HOME("home"),
    BOOKING("booking"),
    SEARCHING("searching"),
    SUCCESS("success"),
    TICKETS("tickets"),
    SETTINGS("settings"),
    ADMIN_HOME("admin_home"),
    ADMIN_DETAIL("admin_detail/{lotId}"),
    ADMIN_QR_SCAN("admin_qr_scan/{lotId}/{mode}"), // mode: checkin or checkout
    ADMIN_BOOKING("admin_booking"),
    ADMIN_SETTINGS("admin_settings"),
    PAYMENT("payment");

    fun withId(id: String): String = route.replace("{lotId}", id)
}
