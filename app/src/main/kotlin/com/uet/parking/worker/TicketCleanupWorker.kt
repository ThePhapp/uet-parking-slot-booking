package com.uet.parking.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.uet.parking.data.repository.ParkingRepository
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class TicketCleanupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val ticketId = inputData.getString("ticketId") ?: return Result.failure()
        val lotId = inputData.getString("lotId") ?: return Result.failure()

        Log.d("TicketCleanupWorker", "Cleaning up ticket: $ticketId from lot: $lotId")

        return try {
            val firestore = FirebaseFirestore.getInstance()
            val repository = ParkingRepository(firestore)

            val ticket = repository.getTicketById(ticketId)
            if (ticket != null) {
                // 1. Cập nhật lưu lượng theo giờ (HourlyLoad)
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val startTime = try { sdf.parse(ticket.startTime ?: "") } catch (e: Exception) { null }
                if (startTime != null) {
                    val dateSdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val date = dateSdf.format(startTime)
                    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(startTime)
                    val shift = when {
                        time < "09:15" -> 1
                        time < "12:30" -> 2
                        time < "15:15" -> 3
                        else -> 4
                    }
                    repository.decrementVehicleCount(lotId, date, shift)
                }

                // 2. Giảm số lượng xe hiện tại
                val lot = repository.getParkingLotById(lotId)
                if (lot != null) {
                    val newCount = ((lot.current ?: 0) - 1).coerceAtLeast(0)
                    repository.updateCurrentOccupancy(lotId, newCount)
                }
            }

            // 3. Xóa vé
            repository.deleteTicket(ticketId)

            Result.success()
        } catch (e: Exception) {
            Log.e("TicketCleanupWorker", "Error cleaning up ticket", e)
            Result.retry()
        }
    }
}
