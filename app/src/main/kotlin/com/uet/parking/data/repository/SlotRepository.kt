package com.uet.parking.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.uet.parking.data.model.Slot
import com.uet.parking.data.model.enums.SlotStatus
import kotlinx.coroutines.tasks.await

class SlotRepository(
    private val firestore: FirebaseFirestore
) {
    private val slotsCollection = firestore.collection("slots")
    private val ticketsCollection = firestore.collection("tickets")

    suspend fun getSlotById(slotId: String): Slot? {
        if (slotId.isBlank()) return null
        return try {
            val doc = slotsCollection.document(slotId).get().await()
            doc.toObject(Slot::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getSlotsByParkingLot(parkingLotId: String): List<Slot> {
        return try {
            val snapshot = slotsCollection
                .whereEqualTo("parkingLotId", parkingLotId)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(Slot::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun initMockSlotsForLot(parkingLotId: String) {
        val existing = getSlotsByParkingLot(parkingLotId)
        if (existing.isNotEmpty()) return

        val mocks = listOf(
            Slot(parkingLotId = parkingLotId, coordinateLabel = "A1", latitude = 21.037001, longitude = 105.782001),
            Slot(parkingLotId = parkingLotId, coordinateLabel = "A2", latitude = 21.037020, longitude = 105.782040),
            Slot(parkingLotId = parkingLotId, coordinateLabel = "B1", latitude = 21.037060, longitude = 105.782080),
            Slot(parkingLotId = parkingLotId, coordinateLabel = "B2", latitude = 21.037090, longitude = 105.782120)
        )

        firestore.runBatch { batch ->
            mocks.forEach { slot ->
                val ref = slotsCollection.document()
                batch.set(ref, slot.copy(id = ref.id))
            }
        }.await()
    }

    /**
     * Gán vị trí cho vé qua Transaction để tránh đụng độ (Race condition).
     */
    suspend fun assignSlotToTicket(parkingLotId: String, ticketId: String, userId: String?): Slot? {
        val availableSlotsSnapshot = slotsCollection
            .whereEqualTo("parkingLotId", parkingLotId)
            .whereEqualTo("status", SlotStatus.AVAILABLE.value)
            .limit(1)
            .get()
            .await()

        if (availableSlotsSnapshot.isEmpty) {
            return null
        }

        val targetSlotDoc = availableSlotsSnapshot.documents.first()
        val slotRef = slotsCollection.document(targetSlotDoc.id)
        val ticketRef = ticketsCollection.document(ticketId)

        return try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(slotRef)
                val currentStatus = snapshot.getString("status")
                
                if (currentStatus != SlotStatus.AVAILABLE.value) {
                    throw Exception("Slot đã được lấy bởi tiến trình khác.")
                }

                // Cập nhật Slot
                transaction.update(slotRef, "status", SlotStatus.OCCUPIED.value)
                transaction.update(slotRef, "userId", userId)

                // Cập nhật Ticket
                transaction.update(ticketRef, "assignedSlotId", slotRef.id)
                transaction.update(ticketRef, "checkedInAt", System.currentTimeMillis())

                snapshot.toObject(Slot::class.java)?.copy(
                    id = slotRef.id,
                    status = SlotStatus.OCCUPIED.value,
                    userId = userId
                )
            }.await()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun releaseSlotFromTicket(ticketId: String) {
        val ticketDoc = ticketsCollection.document(ticketId).get().await()
        val assignedSlotId = ticketDoc.getString("assignedSlotId")
        
        if (!assignedSlotId.isNullOrEmpty()) {
            val slotRef = slotsCollection.document(assignedSlotId)
            firestore.runTransaction { transaction ->
                transaction.update(slotRef, "status", SlotStatus.AVAILABLE.value)
                transaction.update(slotRef, "userId", null)
                // Optionally clear from ticket
                transaction.update(ticketsCollection.document(ticketId), "assignedSlotId", null)
            }.await()
        }
    }
}
