package com.uet.parking.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.uet.parking.MainActivity
import com.uet.parking.data.model.Notification
import com.uet.parking.data.repository.ParkingRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@kotlin.OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
object NotificationHelper {

    private const val CHANNEL_ID = "parking_notifications_v3"
    private const val CHANNEL_NAME = "Parking Notifications"
    private const val CHANNEL_DESC = "Notifications for parking tickets and updates"

    private val repository by lazy { ParkingRepository(FirebaseFirestore.getInstance()) }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showSystemNotification(context: Context, title: String, content: String, notificationId: String? = null) {
        // Kiểm tra cài đặt thông báo trong SharedPreferences cho system notification
        val sharedPrefs = context.getSharedPreferences("parking_prefs", Context.MODE_PRIVATE)
        val notificationsEnabled = sharedPrefs.getBoolean("notifications_enabled", true)

        if (!notificationsEnabled) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (notificationId != null) {
                putExtra("notificationId", notificationId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId?.hashCode() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId?.hashCode() ?: System.currentTimeMillis().toInt(), builder.build())
    }

    private fun showNotification(context: Context, title: String, content: String, ticketId: String?, userId: String?, type: String = "INFO") {
        // Lưu vào Firestore nếu có userId
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                repository.addNotification(
                    Notification(
                        userId = userId,
                        title = title,
                        message = content,
                        type = type
                    )
                )
            }
        }

        showSystemNotification(context, title, content, ticketId)
    }

    fun showBookingSuccess(context: Context, ticketId: String?, userId: String?) {
        showNotification(context, "Đặt vé thành công", "Vé của bạn đã được tạo và chờ xác nhận.", ticketId, userId, "SUCCESS")
    }

    fun showTicketConfirmed(context: Context, ticketId: String?, userId: String?) {
        showNotification(context, "Vé đã được xác nhận", "Vé của bạn đã được xác nhận. Vui lòng xem chi tiết.", ticketId, userId, "SUCCESS")
    }

    fun showTicketCancelled(context: Context, ticketId: String?, userId: String?) {
        showNotification(context, "Vé đã bị hủy", "Vé gửi xe của bạn đã bị hủy.", ticketId, userId, "ERROR")
    }

    fun showCheckInSuccess(context: Context, ticketId: String?, userId: String?) {
        showNotification(context, "Check-in thành công", "Bạn đã check-in thành công vào bãi đỗ.", ticketId, userId, "SUCCESS")
    }

    fun showCheckOutSuccess(context: Context, ticketId: String?, userId: String?) {
        showNotification(context, "Check-out thành công", "Bạn đã lấy xe ra khỏi bãi đỗ thành công.", ticketId, userId, "SUCCESS")
    }

    fun showTicketExpiringSoon(context: Context, ticketId: String?, userId: String?) {
        showNotification(context, "Vé sắp hết hạn", "Vé của bạn sắp hết hạn. Vui lòng chú ý thời gian.", ticketId, userId, "WARNING")
    }

    fun showTicketOverdue(context: Context, ticketId: String?, userId: String?) {
        showNotification(context, "Vé đã quá hạn", "Vé của bạn đã quá hạn. Vui lòng lấy xe ngay.", ticketId, userId, "ERROR")
    }

    fun showPreBooking(context: Context, ticketId: String?, userId: String?) {
        showNotification(context, "Sắp đến giờ gửi xe", "Vé của bạn sẽ bắt đầu sau 30 phút. Vui lòng chuẩn bị đến bãi đỗ.", ticketId, userId, "INFO")
    }

    fun showPostBooking(context: Context, ticketId: String?, userId: String?) {
        showNotification(context, "Sắp quá hạn lấy xe", "Vé của bạn đã kết thúc. Vui lòng lấy xe trong 5 phút để tránh phát sinh phí hoặc vi phạm quy định.", ticketId, userId, "WARNING")
    }
}
