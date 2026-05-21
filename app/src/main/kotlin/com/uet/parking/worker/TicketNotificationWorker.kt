package com.uet.parking.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.uet.parking.data.model.enums.TicketStatus
import com.uet.parking.data.repository.ParkingRepository
import com.uet.parking.utils.NotificationHelper
import kotlinx.coroutines.tasks.await

class TicketNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val ticketId = inputData.getString("ticketId") ?: return Result.failure()
        val type = inputData.getString("type") ?: return Result.failure()

        Log.d("TicketNotificationWorker", "Running worker for ticket: $ticketId, type: $type")

        return try {
            val firestore = FirebaseFirestore.getInstance()
            val repository = ParkingRepository(firestore)

            val document = firestore.collection("tickets").document(ticketId).get().await()
            val statusStr = document.getString("status")
            val status = TicketStatus.values().find { it.value == statusStr }

            if (type == "PRE_BOOKING") {
                if (status == TicketStatus.PENDING || status == TicketStatus.CONFIRMED) {
                    NotificationHelper.showPreBooking(applicationContext, ticketId)
                }
            } else if (type == "POST_BOOKING") {
                if (status == TicketStatus.IN_PROGRESS) {
                    NotificationHelper.showPostBooking(applicationContext, ticketId)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("TicketNotificationWorker", "Error processing notification", e)
            Result.retry()
        }
    }
}
