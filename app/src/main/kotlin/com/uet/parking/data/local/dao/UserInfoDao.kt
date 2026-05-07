package com.uet.parking.data.local.dao

import androidx.room.*
import com.uet.parking.data.model.UserInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface UserInfoDao {
    @Query("SELECT * FROM userInfo WHERE userId = :userId LIMIT 1")
    fun getUserInfoById(userId: Int): Flow<UserInfo?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserInfo(userInfo: UserInfo)

    @Update
    suspend fun updateUserInfo(userInfo: UserInfo)

    @Query("UPDATE userInfo SET debt = :newDebt WHERE userId = :id")
    suspend fun updateDebt(id: Int, newDebt: Double)
}