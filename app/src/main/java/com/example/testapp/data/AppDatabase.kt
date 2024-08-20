package com.example.testapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FavoriteVerse::class, BibleState::class, Bible::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteVerseDao(): FavoriteVerseDao
    abstract fun bibleStateDao(): BibleStateDao
    abstract fun bibleVerseDao(): BibleVerseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bible_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
