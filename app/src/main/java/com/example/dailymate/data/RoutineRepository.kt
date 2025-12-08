package com.example.dailymate.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoutineRepository(private val dailyMateDao: DailyMateDao) {

    val getAllRoutines: LiveData<List<Routine>> = dailyMateDao.getAllRoutines()

    suspend fun insertRoutine(routine: Routine): Long {
        return dailyMateDao.insertRoutine(routine)
    }

    suspend fun deleteRoutine(routine: Routine) {
        dailyMateDao.deleteRoutine(routine)
    }

// 이 함수는 기존 RoutineRepository 클래스의 마지막 부분에 추가하면 됩니다.

    suspend fun updateRoutineCompletion(id: Int, isCompleted: Boolean) {
        dailyMateDao.updateRoutineCompletion(id, isCompleted)
    }
}