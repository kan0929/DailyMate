package com.example.dailymate.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DailyMateViewModelFactory(
    private val userRepository: UserRepository,
    private val routineRepository: RoutineRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DailyMateViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DailyMateViewModel(userRepository, routineRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}