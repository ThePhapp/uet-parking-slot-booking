package com.uet.parking.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.uet.parking.data.model.StudySchedule
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class StudyScheduleRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("lich_hoc")

    fun getSchedulesByUser(userId: String): Flow<List<StudySchedule>> = callbackFlow {
        val listener = collection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val schedules = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(StudySchedule::class.java)?.copy(id = doc.id)
                }.orEmpty()

                trySend(schedules)
            }

        awaitClose { listener.remove() }
    }

    suspend fun addSchedule(schedule: StudySchedule) {
        val doc = collection.document()
        doc.set(schedule.copy(id = doc.id)).await()
    }
}