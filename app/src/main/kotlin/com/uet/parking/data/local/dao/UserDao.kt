package com.uet.parking.data.local.dao

import androidx.room.*
import com.uet.parking.data.model.AdminWithProfile
import com.uet.parking.data.model.User
import com.uet.parking.data.model.UserWithProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM user WHERE userId = :id")
    fun getUserById(id: Int): Flow<User?>

    @Query("SELECT * FROM user WHERE userId = :id LIMIT 1")
    suspend fun getUserByIdSuspend(id: Int): User?

    @Transaction
    @Query("SELECT * FROM user WHERE userId = :userId")
    fun getUserWithProfile(userId: Int): Flow<UserWithProfile?>

    @Transaction
    @Query("SELECT * FROM user WHERE userId = :userId")
    fun getAdminWithProfile(userId: Int): Flow<AdminWithProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
}