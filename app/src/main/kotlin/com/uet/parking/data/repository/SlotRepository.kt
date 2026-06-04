package com.uet.parking.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.uet.parking.data.model.Slot
import com.uet.parking.data.model.enums.SlotStatus
import com.uet.parking.data.model.enums.TicketStatus
import kotlinx.coroutines.tasks.await

class SlotRepository(
    private val firestore: FirebaseFirestore
) {
    private val slotsCollection = firestore.collection("slots")
    private val ticketsCollection = firestore.collection("tickets")

    suspend fun getAvailableSlotByParkingLot(parkingLotId: String): Slot? {
        val snapshot = slotsCollection
            .whereEqualTo("parkingLotId", parkingLotId)
            .whereEqualTo("status", SlotStatus.AVAILABLE.name)
            .limit(1)
            .get()
            .await()
            
        return snapshot.documents.firstOrNull()?.let { doc ->
            doc.toObject(Slot::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun getSlotById(slotId: String): Slot? {
        val doc = slotsCollection.document(slotId).get().await()
        return doc.toObject(Slot::class.java)?.copy(id = doc.id)
    }

    suspend fun getSlotsByParkingLot(parkingLotId: String): List<Slot> {
        val snapshot = slotsCollection
            .whereEqualTo("parkingLotId", parkingLotId)
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Slot::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun assignSlotToTicket(lotId: String, ticketId: String, userId: String?, capacity: Int): Slot? {
        val availableSlotsSnapshot = slotsCollection
            .whereEqualTo("parkingLotId", lotId)
            .whereEqualTo("status", SlotStatus.AVAILABLE.name)
            .get()
            .await()

        if (!availableSlotsSnapshot.isEmpty) {
            for (doc in availableSlotsSnapshot.documents) {
                val slotRef = doc.reference
                val ticketRef = ticketsCollection.document(ticketId)

                try {
                    val assignedSlot = firestore.runTransaction { transaction ->
                        val slotSnapshot = transaction.get(slotRef)
                        val status = slotSnapshot.getString("status")

                        if (status == SlotStatus.AVAILABLE.name) {
                            transaction.update(slotRef, "status", SlotStatus.OCCUPIED.name)
                            transaction.update(slotRef, "userId", userId)
                            transaction.update(slotRef, "updatedAt", System.currentTimeMillis())

                            transaction.update(ticketRef, "assignedSlotId", doc.id)
                            transaction.update(ticketRef, "status", TicketStatus.IN_PROGRESS.value)
                            transaction.update(ticketRef, "checkedInAt", System.currentTimeMillis())

                            slotSnapshot.toObject(Slot::class.java)?.copy(
                                id = doc.id,
                                status = SlotStatus.OCCUPIED.name,
                                userId = userId
                            )
                        } else {
                            null
                        }
                    }.await()

                    if (assignedSlot != null) {
                        return assignedSlot
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SlotRepository", "Transaction failed for slot ${doc.id}", e)
                }
            }
        }

        // Nếu không có slot trống nào có sẵn, thử tạo slot mới nếu chưa đạt sức chứa (capacity)
        val allSlotsSnapshot = slotsCollection.whereEqualTo("parkingLotId", lotId).get().await()
        val totalSlots = allSlotsSnapshot.size()

        if (totalSlots < capacity) {
            val newSlotLabel = generateSlotLabel(totalSlots)
            val newSlotRef = slotsCollection.document()

            val newSlot = Slot(
                id = newSlotRef.id,
                parkingLotId = lotId,
                userId = userId,
                coordinateLabel = newSlotLabel,
                status = SlotStatus.OCCUPIED.name,
                createdAt = System.currentTimeMillis()
            )

            val ticketRef = ticketsCollection.document(ticketId)

            firestore.runTransaction { transaction ->
                transaction.set(newSlotRef, newSlot)
                transaction.update(ticketRef, "assignedSlotId", newSlotRef.id)
                transaction.update(ticketRef, "status", TicketStatus.IN_PROGRESS.value)
                transaction.update(ticketRef, "checkedInAt", System.currentTimeMillis())
            }.await()

            return newSlot
        }

        return null
    }

    private fun generateSlotLabel(currentIndex: Int): String {
        // Tạo label thông minh: 0-9 -> A1-A10, 10-19 -> B1-B10, v.v.
        val rowChar = 'A' + (currentIndex / 10)
        val colNum = (currentIndex % 10) + 1
        return "$rowChar$colNum"
    }

    suspend fun releaseSlotFromTicket(ticketId: String) {
        val ticketDoc = ticketsCollection.document(ticketId).get().await()
        val assignedSlotId = ticketDoc.getString("assignedSlotId")
        
        if (assignedSlotId != null) {
            val slotRef = slotsCollection.document(assignedSlotId)
            firestore.runTransaction { transaction ->
                val slotSnapshot = transaction.get(slotRef)
                val currentStatus = slotSnapshot.getString("status")
                
                if (currentStatus == SlotStatus.OCCUPIED.name) {
                    transaction.update(slotRef, "status", SlotStatus.AVAILABLE.name)
                    transaction.update(slotRef, "userId", null)
                    transaction.update(slotRef, "updatedAt", System.currentTimeMillis())
                }
                
                val ticketRef = ticketsCollection.document(ticketId)
                transaction.update(ticketRef, "status", TicketStatus.DONE.value)
            }.await()
        } else {
            val ticketRef = ticketsCollection.document(ticketId)
            ticketRef.update("status", TicketStatus.DONE.value).await()
        }
    }

    suspend fun initMockSlotsForLot(parkingLotId: String) {
        val existingSlots = getSlotsByParkingLot(parkingLotId)
        if (existingSlots.isNotEmpty()) return

        val mockSlots = listOf(
            Slot(parkingLotId = parkingLotId, coordinateX = 10f, coordinateY = 20f, coordinateLabel = "A1"),
            Slot(parkingLotId = parkingLotId, coordinateX = 20f, coordinateY = 20f, coordinateLabel = "A2"),
            Slot(parkingLotId = parkingLotId, coordinateX = 10f, coordinateY = 40f, coordinateLabel = "B1"),
            Slot(parkingLotId = parkingLotId, coordinateX = 20f, coordinateY = 40f, coordinateLabel = "B2")
        )

        val batch = firestore.batch()
        mockSlots.forEach { slot ->
            val ref = slotsCollection.document()
            batch.set(ref, slot.copy(id = ref.id, createdAt = System.currentTimeMillis()))
        }
        batch.commit().await()
    }
}
