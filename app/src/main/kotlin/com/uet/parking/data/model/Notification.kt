package com.uet.parking.data.model

import com.google.firebase.Timestamp

data class Notification(
    val notificationId: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val isRead: Boolean = false,
    val type: String = "INFO" // INFO, SUCCESS, WARNING, ERROR
)
