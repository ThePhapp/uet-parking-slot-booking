package com.uet.parking.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uet.parking.data.model.ParkingLot
import kotlinx.coroutines.flow.Flow

@Dao
interface ParkingLotDao {
    @Query("SELECT * FROM parkinglot")
    fun getAllParkingLots(): Flow<List<ParkingLot>>

    @Query("SELECT * FROM parkinglot WHERE parkingId = :id")
    suspend fun getParkingLotById(id: Int): ParkingLot?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(parkingLots: List<ParkingLot>)

    @Query("UPDATE parkinglot SET current = :current WHERE parkingId = :id")
    suspend fun updateCurrentOccupancy(id: Int, current: Int)
}
