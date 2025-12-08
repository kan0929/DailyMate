package com.example.dailymate.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DailyMateDao {

    @Insert
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): User?

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    suspend fun isEmailRegistered(email: String): Boolean

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Int)

    @Query("SELECT * FROM routines ORDER BY id DESC")
    fun getAllRoutines(): LiveData<List<Routine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: Routine): Long

    @Query("UPDATE routines SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateRoutineCompletion(id: Int, isCompleted: Boolean)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Delete
    suspend fun deleteRoutine(routine: Routine)

    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :passwordHash LIMIT 1")
    suspend fun getUserByEmailAndPasswordHash(email: String, passwordHash: String): User?

    // 🚀 [추가] 비밀번호 업데이트 쿼리
    @Query("UPDATE users SET passwordHash = :newPasswordHash WHERE id = :userId")
    suspend fun updatePasswordHash(userId: Int, newPasswordHash: String): Int
}