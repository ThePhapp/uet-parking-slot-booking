package com.uet.parking.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uet.parking.data.model.Notification
import com.uet.parking.data.repository.ParkingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(private val repository: ParkingRepository) : ViewModel() {
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _unreadCount = mutableStateOf(0)
    val unreadCount: State<Int> = _unreadCount

    private var currentUserId: String? = null

    fun loadNotifications(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            repository.getNotificationsForUser(userId).collect { list ->
                _notifications.value = list
                _unreadCount.value = list.count { !it.isRead }
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            repository.markAllNotificationsAsRead(userId)
        }
    }
}
