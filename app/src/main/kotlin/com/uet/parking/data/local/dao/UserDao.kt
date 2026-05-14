package com.uet.parking.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uet.parking.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM user WHERE userId = :id")
    fun getUserById(id: Int): Flow<User?>

    @Query("SELECT * FROM user WHERE userId = :id LIMIT 1")
    suspend fun getUserByIdSuspend(id: Int): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("UPDATE user SET debt = :newDebt WHERE userId = :id")
    suspend fun updateDebt(id: Int, newDebt: Double)
}
