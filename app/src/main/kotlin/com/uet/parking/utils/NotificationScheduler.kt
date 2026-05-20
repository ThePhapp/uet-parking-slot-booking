package com.uet.parking.utils

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.uet.parking.data.model.Ticket
import com.uet.parking.worker.TicketNotificationWorker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    fun schedulePreBookingNotification(context: Context, ticket: Ticket) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val startTime = try { sdf.parse(ticket.startTime ?: "") } catch (e: Exception) { null }

        if (startTime != null && ticket.ticketId != null) {
            val now = Date()
            val triggerTime = startTime.time - (30 * 60 * 1000) // 30 mins before start

            val delay = triggerTime - now.time
            if (delay > 0) {
                val data = Data.Builder()
                    .putString("ticketId", ticket.ticketId)
                    .putString("type", "PRE_BOOKING")
                    .build()

                val request = OneTimeWorkRequestBuilder<TicketNotificationWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .addTag("PRE_BOOKING_${ticket.ticketId}")
                    .build()

                WorkManager.getInstance(context).enqueue(request)
            }
        }
    }

    fun schedulePostBookingNotification(context: Context, ticket: Ticket) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val endTime = try { sdf.parse(ticket.endTime ?: "") } catch (e: Exception) { null }

        if (endTime != null && ticket.ticketId != null) {
            val now = Date()
            val triggerTime = endTime.time + (25 * 60 * 1000) // 25 mins after end

            val delay = triggerTime - now.time
            if (delay > 0) {
                val data = Data.Builder()
                    .putString("ticketId", ticket.ticketId)
                    .putString("type", "POST_BOOKING")
                    .build()

                val request = OneTimeWorkRequestBuilder<TicketNotificationWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .addTag("POST_BOOKING_${ticket.ticketId}")
                    .build()

                WorkManager.getInstance(context).enqueue(request)
            }
        }
    }

    fun cancelNotifications(context: Context, ticketId: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag("PRE_BOOKING_$ticketId")
        WorkManager.getInstance(context).cancelAllWorkByTag("POST_BOOKING_$ticketId")
    }
}
