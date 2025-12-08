package com.example.dailymate.data

class UserRepository(private val dailyMateDao: DailyMateDao) {

    suspend fun signup(user: User): Long {
        return dailyMateDao.insertUser(user)
    }

    suspend fun getUserByEmail(email: String): User? {
        return dailyMateDao.getUserByEmail(email)
    }

    suspend fun getUserById(userId: Int): User? {
        return dailyMateDao.getUserById(userId)
    }

    suspend fun isEmailRegistered(email: String): Boolean {
        return dailyMateDao.isEmailRegistered(email)
    }

    suspend fun deleteUser(userId: Int){
        dailyMateDao.deleteUserById(userId)
    }

    suspend fun deleteAllUsers() {
        dailyMateDao.deleteAllUsers()
    }

    suspend fun getUserByEmailAndPasswordHash(email: String, passwordHash: String): User? {
        return dailyMateDao.getUserByEmailAndPasswordHash(email, passwordHash)
    }
    suspend fun updatePassword(userId: Int, newPasswordHash: String): Int {
        return dailyMateDao.updatePasswordHash(userId, newPasswordHash)
    }
}