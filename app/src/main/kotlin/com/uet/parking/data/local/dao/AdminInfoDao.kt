package com.uet.parking.data.local.dao

import androidx.room.*
import com.uet.parking.data.model.AdminInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminInfoDao {
    @Query("SELECT * FROM adminInfo WHERE userId = :userId LIMIT 1")
    fun getAdminInfoById(userId: Int): Flow<AdminInfo?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdminInfo(adminInfo: AdminInfo)

    @Update
    suspend fun updateAdminInfo(adminInfo: AdminInfo)
}
