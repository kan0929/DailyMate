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

    // [수정] routineId -> id
    @Query("SELECT * FROM routines ORDER BY id DESC")
    fun getAllRoutines(): LiveData<List<Routine>>

    // [수정] routineId -> id
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: Routine): Long

    // [수정] routineId -> id
    @Query("UPDATE routines SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateRoutineCompletion(id: Int, isCompleted: Boolean)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :passwordHash")
    suspend fun getUserByEmailAndPasswordHash(email: String, passwordHash: String): User?
}