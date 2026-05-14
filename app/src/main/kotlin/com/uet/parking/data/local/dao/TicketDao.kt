package com.uet.parking.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.uet.parking.data.model.Ticket
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Query("SELECT * FROM ticket WHERE userId = :userId")
    fun getTicketsByUserId(userId: Int): Flow<List<Ticket>>

    @Query("SELECT * FROM ticket WHERE ticketId = :ticketId LIMIT 1")
    suspend fun getTicketById(ticketId: Int): Ticket?

    @Insert
    suspend fun insertTicket(ticket: Ticket)

    @Query("UPDATE ticket SET status = :status WHERE ticketId = :ticketId")
    suspend fun updateTicketStatus(ticketId: Int, status: String)

    @Delete
    suspend fun deleteTicket(ticket: Ticket)
}
