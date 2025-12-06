package com.example.dailymate.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// ⭐ [수정] version = 1을 version = 2로 올립니다.
@Database(entities = [User::class, Routine::class], version = 2, exportSchema = false)
abstract class DailyMateDatabase : RoomDatabase() {

    abstract fun dailyMateDao(): DailyMateDao

    companion object {
        @Volatile
        private var INSTANCE: DailyMateDatabase? = null

        fun getDatabase(context: Context): DailyMateDatabase {

            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DailyMateDatabase::class.java,
                    "daily_mate_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                // 반환
                instance
            }
        }
    }
}