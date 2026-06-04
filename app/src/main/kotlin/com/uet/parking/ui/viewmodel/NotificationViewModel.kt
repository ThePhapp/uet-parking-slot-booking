package com.uet.parking.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uet.parking.data.model.Notification
import com.uet.parking.data.repository.ParkingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotificationViewModel(private val repository: ParkingRepository) : ViewModel() {
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _unreadCount = mutableStateOf(0)
    val unreadCount: State<Int> = _unreadCount
    
    // Trạng thái kiểm soát việc hiển thị Badge trên chuông
    private val _showBadge = mutableStateOf(false)
    val showBadge: State<Boolean> = _showBadge
    
    private var currentUserId: String? = null
    private var job: Job? = null

    fun loadNotifications(userId: String) {
        if (currentUserId == userId && job?.isActive == true) return
        currentUserId = userId
        job?.cancel()
        
        job = viewModelScope.launch {
            launch {
                combine(
                    repository.getNotificationsForUser(userId),
                    repository.getGlobalNotifications()
                ) { userNotifs, globalNotifs ->
                    (userNotifs + globalNotifs)
                        .sortedByDescending { it.timestamp }
                        .take(10)
                }.collect { list ->
                    _notifications.value = list
                }
            }

            val countFlow = repository.getUnreadCount(userId)

            var isFirstEmission = true
            countFlow.collect { count ->
                // Nếu số lượng chưa đọc tăng lên (có tin mới), hiển thị Badge
                if (!isFirstEmission && count > _unreadCount.value) {
                    _showBadge.value = true
                } else if (isFirstEmission && count > 0) {
                    // Lần đầu tải app nếu có tin chưa đọc thì vẫn hiện Badge
                    _showBadge.value = true
                }
                
                _unreadCount.value = count
                isFirstEmission = false
                
                // Nếu không còn tin chưa đọc nào, ẩn Badge
                if (count == 0) {
                    _showBadge.value = false
                }
            }
        }
    }

    fun markAsSeen() {
        // Khi người dùng mở menu, ẩn Badge trên chuông
        _showBadge.value = false
    }

    fun clear() {
        currentUserId = null
        job?.cancel()
        _notifications.value = emptyList()
        _unreadCount.value = 0
        _showBadge.value = false
    }

    fun markAsRead(notificationId: String) {
        val notification = _notifications.value.find { it.notificationId == notificationId }
        if (notification != null && !notification.isRead) {
            _unreadCount.value = (_unreadCount.value - 1).coerceAtLeast(0)
            
            _notifications.update { currentList ->
                currentList.map { 
                    if (it.notificationId == notificationId) it.copy(isRead = true) else it 
                }
            }
        }

        viewModelScope.launch {
            try {
                repository.markNotificationAsRead(notificationId)
            } catch (e: Exception) {}
        }
    }

    fun markAllAsRead() {
        val userId = currentUserId ?: return
        
        // Optimistic update: Update local state immediately for better UX
        _notifications.update { currentList ->
            currentList.map { it.copy(isRead = true) }
        }
        _unreadCount.value = 0
        
        viewModelScope.launch {
            try {
                repository.markAllNotificationsAsRead(userId)
            } catch (e: Exception) {
                // If remote update fails, the real-time listener will eventually sync it back
            }
        }
    }
}
