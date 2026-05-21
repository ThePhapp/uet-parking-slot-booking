package com.uet.parking.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.uet.parking.MainActivity

@kotlin.OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
object NotificationHelper {

    private const val CHANNEL_ID = "parking_notifications_v2"
    private const val CHANNEL_NAME = "Parking Notifications"
    private const val CHANNEL_DESC = "Notifications for parking tickets and updates"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(context: Context, title: String, content: String, ticketId: String?) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (ticketId != null) {
                putExtra("ticketId", ticketId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            ticketId?.hashCode() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(ticketId?.hashCode() ?: System.currentTimeMillis().toInt(), builder.build())
    }

    fun showBookingSuccess(context: Context, ticketId: String?) {
        showNotification(context, "Đặt vé thành công", "Vé của bạn đã được tạo và chờ xác nhận.", ticketId)
    }

    fun showTicketConfirmed(context: Context, ticketId: String?) {
        showNotification(context, "Vé đã được xác nhận", "Vé của bạn đã được xác nhận. Vui lòng xem chi tiết.", ticketId)
    }

    fun showTicketCancelled(context: Context, ticketId: String?) {
        showNotification(context, "Vé đã bị hủy", "Vé gửi xe của bạn đã bị hủy.", ticketId)
    }

    fun showCheckInSuccess(context: Context, ticketId: String?) {
        showNotification(context, "Check-in thành công", "Bạn đã check-in thành công vào bãi đỗ.", ticketId)
    }

    fun showCheckOutSuccess(context: Context, ticketId: String?) {
        showNotification(context, "Check-out thành công", "Bạn đã lấy xe ra khỏi bãi đỗ thành công.", ticketId)
    }

    fun showTicketExpiringSoon(context: Context, ticketId: String?) {
        showNotification(context, "Vé sắp hết hạn", "Vé của bạn sắp hết hạn. Vui lòng chú ý thời gian.", ticketId)
    }

    fun showTicketOverdue(context: Context, ticketId: String?) {
        showNotification(context, "Vé đã quá hạn", "Vé của bạn đã quá hạn. Vui lòng lấy xe ngay.", ticketId)
    }
    
    fun showPreBooking(context: Context, ticketId: String?) {
        showNotification(context, "Sắp đến giờ gửi xe", "Vé của bạn sẽ bắt đầu sau 30 phút. Vui lòng chuẩn bị đến bãi đỗ.", ticketId)
    }

    fun showPostBooking(context: Context, ticketId: String?) {
        showNotification(context, "Sắp quá hạn lấy xe", "Vé của bạn đã kết thúc. Vui lòng lấy xe trong 5 phút để tránh phát sinh phí hoặc vi phạm quy định.", ticketId)
    }
}
