package com.uet.parking.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uet.parking.data.model.HourlyLoad

@Dao
interface HourlyLoadDao {
    @Query("SELECT * FROM hourlyloads WHERE parkingId = :parkingId AND date = :date AND shift = :shift LIMIT 1")
    suspend fun getLoad(parkingId: Int, date: String, shift: Int): HourlyLoad?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertOrUpdate(hourlyLoad: HourlyLoad)

    @Query("UPDATE hourlyloads SET vehicleCount = vehicleCount + 1 WHERE parkingId = :parkingId AND date = :date AND shift = :shift")
    suspend fun incrementVehicleCount(parkingId: Int, date: String, shift: Int): Int
}