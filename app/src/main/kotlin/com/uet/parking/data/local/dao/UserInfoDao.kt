package com.uet.parking.data.local.dao

import androidx.room.*
import com.uet.parking.data.model.UserInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface UserInfoDao {
    @Query("SELECT * FROM user_info WHERE userId = :userId LIMIT 1")
    fun getUserInfoById(userId: Int): Flow<UserInfo?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserInfo(userInfo: UserInfo)

    @Update
    suspend fun updateUserInfo(userInfo: UserInfo)
}
