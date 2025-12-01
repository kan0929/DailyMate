package com.example.dailymate.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoutineRepository(private val dailyMateDao: DailyMateDao) {

    val getAllRoutines: LiveData<List<Routine>> = dailyMateDao.getAllRoutines()

    suspend fun insertRoutine(routine: Routine): Long {
        return dailyMateDao.insertRoutine(routine)
    }

    suspend fun updateRoutineCompletion(id: Int, isCompleted: Boolean) {
        dailyMateDao.updateRoutineCompletion(id, isCompleted)
    }
}