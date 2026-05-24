package com.uet.parking.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class NotificationViewModel : ViewModel() {
    private val _hasNotification = mutableStateOf(false)
    val hasNotification: State<Boolean> = _hasNotification

    private val _notificationCount = mutableStateOf(0)
    val notificationCount: State<Int> = _notificationCount

    fun setHasNotification(has: Boolean) {
        _hasNotification.value = has
    }

    fun setNotificationCount(count: Int) {
        _notificationCount.value = count
        _hasNotification.value = count > 0
    }

    fun clearNotifications() {
        _notificationCount.value = 0
        _hasNotification.value = false
    }

    // Giả lập nhận thông báo mới
    fun simulateNewNotification() {
        setNotificationCount(_notificationCount.value + 1)
    }
}
