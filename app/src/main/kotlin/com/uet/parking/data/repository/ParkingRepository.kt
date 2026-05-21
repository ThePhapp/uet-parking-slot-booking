package com.uet.parking.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import com.uet.parking.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await

class ParkingRepository(
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")
    private val parkingLotsCollection = firestore.collection("parkingLots")
    private val ticketsCollection = firestore.collection("tickets")
    private val userInfoCollection = firestore.collection("userInfo")
    private val adminInfoCollection = firestore.collection("adminInfo")
    private val hourlyLoadsCollection = firestore.collection("hourlyLoads")

    // --- User ---
    suspend fun getUserByEmail(email: String): User? {
        val snapshot = usersCollection.whereEqualTo("email", email).limit(1).get().await()
        return snapshot.documents.firstOrNull()?.let { doc ->
            doc.toObject(User::class.java)?.copy(userId = doc.id)
        }
    }

    suspend fun getUserByIdSuspend(userId: String): User? {
        if (userId.isBlank()) return null
        return try {
            val document = usersCollection.document(userId).get().await()
            document.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createUser(user: User): String {
        val docRef = if (user.userId != null && user.userId.isNotEmpty()) {
            usersCollection.document(user.userId)
        } else {
            usersCollection.document()
        }
        val finalUser = user.copy(userId = docRef.id)
        docRef.set(finalUser).await()
        return docRef.id
    }

    suspend fun updateUserName(userId: String, newName: String) {
        usersCollection.document(userId).update("name", newName).await()
    }

    suspend fun updatePassword(userId: String, newPass: String) {
        usersCollection.document(userId).update("password", newPass).await()
    }

    // --- User Info & Debt ---
    suspend fun createUserInfo(userInfo: UserInfo) {
        userInfoCollection.document(userInfo.userId).set(userInfo).await()
    }

    suspend fun getUserInfoByIdOnce(userId: String): UserInfo? {
        return userInfoCollection.document(userId).get().await().toObject(UserInfo::class.java)
    }

    suspend fun updateDebt(userId: String, newDebt: Double) {
        val data = mapOf("debt" to newDebt, "userId" to userId)
        userInfoCollection.document(userId).set(data, SetOptions.merge()).await()
    }

    fun getUserWithProfile(userId: String): Flow<UserWithProfile?> {
        if (userId.isBlank()) return kotlinx.coroutines.flow.flowOf(null)
        val userFlow = usersCollection.document(userId).snapshots().map { doc ->
            doc.toObject(User::class.java)?.copy(userId = doc.id)
        }
        val infoFlow = userInfoCollection.document(userId).snapshots().map { it.toObject(UserInfo::class.java) }
        
        return userFlow.combine(infoFlow) { user, info ->
            if (user != null) UserWithProfile(user, info) else null
        }
    }

    // --- Admin Info & KPI ---
    suspend fun createAdminInfo(adminInfo: AdminInfo) {
        adminInfoCollection.document(adminInfo.userId).set(adminInfo).await()
    }

    suspend fun incrementKPI(adminId: String) {
        adminInfoCollection.document(adminId)
            .set(mapOf("kpi" to FieldValue.increment(1)), SetOptions.merge()).await()
    }

    fun getAdminWithProfile(adminId: String): Flow<AdminWithProfile?> {
        if (adminId.isBlank()) return kotlinx.coroutines.flow.flowOf(null)
        val userFlow = usersCollection.document(adminId).snapshots().map { doc ->
            doc.toObject(User::class.java)?.copy(userId = doc.id)
        }
        val adminInfoFlow = adminInfoCollection.document(adminId).snapshots().map { it.toObject(AdminInfo::class.java) }
        
        return userFlow.combine(adminInfoFlow) { user, adminInfo ->
            if (user != null) AdminWithProfile(user, adminInfo) else null
        }
    }

    // --- Parking Lot ---
    fun getAllParkingLots(): Flow<List<ParkingLot>> {
        return parkingLotsCollection.snapshots().map { snapshot ->
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(ParkingLot::class.java)?.copy(parkingId = doc.id)
            }
        }
    }

    suspend fun getParkingLotById(id: String): ParkingLot? {
        val doc = parkingLotsCollection.document(id).get().await()
        return doc.toObject(ParkingLot::class.java)?.copy(parkingId = doc.id)
    }

    suspend fun updateCurrentOccupancy(lotId: String, current: Int) {
        parkingLotsCollection.document(lotId).update("current", current).await()
    }

    // --- Ticket ---
    fun getAllTickets(): Flow<List<Ticket>> {
        return ticketsCollection.snapshots().map { snapshot ->
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Ticket::class.java)?.copy(ticketId = doc.id)
            }
        }
    }

    suspend fun getTicketsByUserIdOnce(userId: String): List<Ticket> {
        val snapshot = ticketsCollection.whereEqualTo("userId", userId).get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Ticket::class.java)?.copy(ticketId = doc.id)
        }
    }

    suspend fun getTicketById(ticketId: String): Ticket? {
        val doc = ticketsCollection.document(ticketId).get().await()
        return doc.toObject(Ticket::class.java)?.copy(ticketId = doc.id)
    }

    suspend fun createTicket(ticket: Ticket): String {
        val docRef = ticketsCollection.document()
        val finalTicket = ticket.copy(ticketId = docRef.id)
        docRef.set(finalTicket).await()
        return docRef.id
    }

    suspend fun updateTicketStatus(ticketId: String, status: String) {
        ticketsCollection.document(ticketId).update("status", status).await()
    }

    suspend fun deleteTicket(ticketId: String) {
        ticketsCollection.document(ticketId).delete().await()
    }

    // --- Hourly Load ---
    suspend fun getLoad(parkingId: String, date: String, shift: Int): HourlyLoad? {
        val snapshot = hourlyLoadsCollection
            .whereEqualTo("parkingId", parkingId)
            .whereEqualTo("date", date)
            .whereEqualTo("shift", shift)
            .limit(1)
            .get()
            .await()
        return snapshot.documents.firstOrNull()?.let { doc ->
            doc.toObject(HourlyLoad::class.java)?.copy(loadId = doc.id)
        }
    }

    suspend fun updateHourlyLoad(load: HourlyLoad) {
        val safeDate = load.date?.replace("/", "-") ?: "unknown"
        val docId = "${load.parkingId}_${safeDate}_${load.shift}"
        hourlyLoadsCollection.document(docId).set(load, SetOptions.merge()).await()
    }

    suspend fun incrementVehicleCount(parkingId: String, date: String, shift: Int) {
        val safeDate = date.replace("/", "-")
        val docId = "${parkingId}_${safeDate}_${shift}"
        hourlyLoadsCollection.document(docId).set(
            mapOf("vehicleCount" to FieldValue.increment(1)),
            SetOptions.merge()
        ).await()
    }

    suspend fun decrementVehicleCount(parkingId: String, date: String, shift: Int) {
        val safeDate = date.replace("/", "-")
        val docId = "${parkingId}_${safeDate}_${shift}"
        hourlyLoadsCollection.document(docId).update("vehicleCount", FieldValue.increment(-1)).await()
    }

    suspend fun getShiftFlowLoad(parkingId: String, timeString: String): Pair<Int, Int> {
        val incomingSnapshot = ticketsCollection
            .whereEqualTo("parkingId", parkingId)
            .whereEqualTo("startTime", timeString)
            .get().await()
        val outgoingSnapshot = ticketsCollection
            .whereEqualTo("parkingId", parkingId)
            .whereEqualTo("endTime", timeString)
            .get().await()
        return Pair(incomingSnapshot.size(), outgoingSnapshot.size())
    }
}
