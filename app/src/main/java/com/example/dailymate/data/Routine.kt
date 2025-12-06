package com.example.dailymate.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class Routine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val name: String,
    val days: String,
    val isCompleted: Boolean = false,
    val goalAmount: String,
    val routineType: String,
    val startDate: Long,
    val endDate: Long
)