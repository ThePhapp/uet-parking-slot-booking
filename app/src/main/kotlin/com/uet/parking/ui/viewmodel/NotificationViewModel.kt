package com.uet.parking.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uet.parking.data.model.Notification
import com.uet.parking.data.repository.ParkingRepository
import com.uet.parking.utils.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotificationViewModel(application: Application, private val repository: ParkingRepository) : AndroidViewModel(application) {
    
    private var currentUserId: String? = null
    private var job: Job? = null
    private var isFirstLoad = true
    
    // Lưu danh sách ID đã hiển thị thông báo hệ thống trong phiên làm việc này để tránh lặp lại
    private val displayedNotificationIds = mutableSetOf<String>()

    fun loadNotifications(userId: String) {
        if (currentUserId == userId && job?.isActive == true) return
        currentUserId = userId
        job?.cancel()
        
        job = viewModelScope.launch {
            isFirstLoad = true
            combine(
                repository.getNotificationsForUser(userId),
                repository.getGlobalNotifications()
            ) { userNotifs, globalNotifs ->
                (userNotifs + globalNotifs)
            }.collect { list ->
                list.forEach { notif ->
                    // Chỉ gửi thông báo hệ thống nếu tin chưa đọc và chưa được hiển thị trong phiên này
                    if (!notif.isRead && notif.notificationId != null && !displayedNotificationIds.contains(notif.notificationId)) {
                        if (!isFirstLoad) {
                            NotificationHelper.showSystemNotification(
                                getApplication(),
                                notif.title,
                                notif.message,
                                notif.notificationId
                            )
                        }
                        displayedNotificationIds.add(notif.notificationId!!)
                    }
                }
                isFirstLoad = false
            }
        }
    }

    fun clear() {
        currentUserId = null
        job?.cancel()
        displayedNotificationIds.clear()
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                repository.markNotificationAsRead(notificationId)
            } catch (e: Exception) {}
        }
    }
}
