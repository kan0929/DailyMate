package com.example.dailymate.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*

class DailyMateViewModel(
    private val userRepository: UserRepository,
    private val routineRepository: RoutineRepository
) : ViewModel() {

    val getAllRoutines: LiveData<List<Routine>> = routineRepository.getAllRoutines

    private val _currentUserId = MutableStateFlow<Int?>(null)
    private val _currentUserName = MutableStateFlow("사용자")
    val currentUserName: StateFlow<String> = _currentUserName.asStateFlow()

    private val allRoutinesFlow = routineRepository.getAllRoutines.asFlow()

    fun setUserId(userId: Int) {
        _currentUserId.value = userId
    }

    fun loadUserName(userId: Int) {
        viewModelScope.launch {
            val user = userRepository.getUserById(userId)
            _currentUserName.value = user?.fullName ?: "사용자"
        }
    }

    fun setCurrentUserName(name: String) {
        _currentUserName.value = name
    }

    val dueTodayRoutines: StateFlow<List<Routine>> = combine(
        _currentUserId.filterNotNull(),
        allRoutinesFlow
    ) { userId, allRoutines ->
        val today = LocalDate.now().dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.KOREAN)

        allRoutines.filter { routine ->
            if (routine.userId != userId) return@filter false

            val isFixedType = routine.days == "DAILY" || routine.days == "WEEKLY" || routine.days == "MONTHLY"
            val isDayOfWeekRoutineDue = routine.days.contains(today)

            isFixedType || isDayOfWeekRoutineDue
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val dailyRoutines: StateFlow<List<Routine>> = dueTodayRoutines.map { routines ->
        routines.filter { it.days == "DAILY" }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val weeklyRoutines: StateFlow<List<Routine>> = dueTodayRoutines.map { routines ->
        routines.filter { it.days == "WEEKLY" }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val monthlyRoutines: StateFlow<List<Routine>> = dueTodayRoutines.map { routines ->
        routines.filter { it.days == "MONTHLY" }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )


    // ⭐ [수정] 진행도 계산을 dailyRoutines를 기반으로 변경
    val progress: StateFlow<Float> = dailyRoutines.map { routines ->
        if (routines.isEmpty()) 0f
        else {
            val completedCount = routines.count { it.isCompleted }
            completedCount.toFloat() / routines.size
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    fun signup(user: User, onSuccess: (Int, String) -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            if (userRepository.isEmailRegistered(user.email)) {
                onError()
            } else {
                val newId = userRepository.signup(user).toInt()
                onSuccess(newId, user.fullName)
            }
        }
    }

    fun signin(
        email: String,
        passwordHash: String,
        onSuccess: (Int, String) -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            val user = userRepository.getUserByEmailAndPasswordHash(email, passwordHash)
            if (user != null) {
                onSuccess(user.id, user.fullName)
            } else {
                onError()
            }
        }
    }

    fun deleteUser(userId: Int) = viewModelScope.launch {
        userRepository.deleteUser(userId)
    }
    fun addRoutine(routine: Routine) {
        viewModelScope.launch(Dispatchers.IO) {
            routineRepository.insertRoutine(routine)
        }
    }

    fun updateRoutineCompletion(id: Int, isCompleted: Boolean) = viewModelScope.launch {
        routineRepository.updateRoutineCompletion(id, isCompleted)
    }

    fun toggleRoutineCompletion(routine: Routine) = viewModelScope.launch {
        routineRepository.updateRoutineCompletion(routine.id, !routine.isCompleted)
    }

    // ⭐ [추가] 루틴 삭제 함수
    fun deleteRoutine(routine: Routine) {
        viewModelScope.launch(Dispatchers.IO) {
            routineRepository.deleteRoutine(routine)
        }
    }

    fun deleteAllUsers() {
        viewModelScope.launch {
            userRepository.deleteAllUsers()
        }
    }
}