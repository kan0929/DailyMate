package com.example.dailymate.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy

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

    // ⭐ [수정된 부분] 쿼리에서 'id' 대신 'routineId'를 사용하도록 수정
    @Query("SELECT * FROM routines ORDER BY routineId DESC")
    fun getAllRoutines(): LiveData<List<Routine>>

    // NOTE: updateRoutineCompletion 쿼리도 Routine의 기본 키인 routineId를 사용해야 함
    @Query("UPDATE routines SET isCompleted = :isCompleted WHERE routineId = :id")
    suspend fun updateRoutineCompletion(id: Int, isCompleted: Boolean)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :passwordHash")
    suspend fun getUserByEmailAndPasswordHash(email: String, passwordHash: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: Routine): Long
}