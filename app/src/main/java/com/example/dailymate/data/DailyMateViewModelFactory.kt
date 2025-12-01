package com.example.dailymate.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class, Routine::class], version = 1, exportSchema = false)
abstract class DailyMateDatabase : RoomDatabase() {
    abstract fun dailyMateDao(): DailyMateDao

    companion object {
        @Volatile
        private var Instance: DailyMateDatabase? = null

        fun getDatabase(context: Context): DailyMateDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, DailyMateDatabase::class.java, "daily_mate_db")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}