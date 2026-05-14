package com.uet.parking.data.repository

import com.uet.parking.data.local.dao.*
import com.uet.parking.data.model.*
import kotlinx.coroutines.flow.Flow

class ParkingRepository(
    private val userDao: UserDao,
    private val ticketDao: TicketDao,
    private val parkingLotDao: ParkingLotDao,
    private val hourlyLoadDao: HourlyLoadDao,
    private val userInfoDao: UserInfoDao,
    private val adminInfoDao: AdminInfoDao
) {
    // User
    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)
    fun getUserById(id: Int): Flow<User?> = userDao.getUserById(id)
    suspend fun getUserByIdSuspend(id: Int): User? = userDao.getUserByIdSuspend(id)

    // Cập nhật nợ (Debt) vào bảng userInfo
    suspend fun updateDebt(id: Int, newDebt: Double) = userInfoDao.updateDebt(id, newDebt)

    // User Profiles
    fun getUserWithProfile(userId: Int): Flow<UserWithProfile?> = userDao.getUserWithProfile(userId)
    fun getAdminWithProfile(userId: Int): Flow<AdminWithProfile?> = userDao.getAdminWithProfile(userId)

    // User Info
    fun getUserInfoById(userId: Int): Flow<UserInfo?> = userInfoDao.getUserInfoById(userId)
    suspend fun insertUserInfo(userInfo: UserInfo) = userInfoDao.insertUserInfo(userInfo)

    // Admin Info
    fun getAdminInfoById(userId: Int): Flow<AdminInfo?> = adminInfoDao.getAdminInfoById(userId)
    suspend fun insertAdminInfo(adminInfo: AdminInfo) = adminInfoDao.insertAdminInfo(adminInfo)
    suspend fun incrementKPI(userId: Int) = adminInfoDao.incrementKPI(userId)

    // Parking Lot
    fun getAllParkingLots(): Flow<List<ParkingLot>> = parkingLotDao.getAllParkingLots()
    suspend fun getParkingLotById(id: Int): ParkingLot? = parkingLotDao.getParkingLotById(id)
    suspend fun updateCurrentOccupancy(id: Int, current: Int) = parkingLotDao.updateCurrentOccupancy(id, current)

    // Ticket
    fun getAllTickets(): Flow<List<Ticket>> = ticketDao.getAllTickets()
    suspend fun getTicketById(ticketId: Int): Ticket? = ticketDao.getTicketById(ticketId)
    suspend fun insertTicket(ticket: Ticket) = ticketDao.insertTicket(ticket)
    suspend fun updateTicketStatus(ticketId: Int, status: String) = ticketDao.updateTicketStatus(ticketId, status)
    suspend fun deleteTicket(ticket: Ticket) = ticketDao.deleteTicket(ticket)

    // Hourly Load
    suspend fun getLoad(parkingId: Int, date: String, shift: Int): HourlyLoad? =
        hourlyLoadDao.getLoad(parkingId, date, shift)
}