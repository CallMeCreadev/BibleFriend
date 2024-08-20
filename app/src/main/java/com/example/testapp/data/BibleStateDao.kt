package com.example.testapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BibleStateDao {
    @Query("SELECT * FROM bible_state WHERE id = :id LIMIT 1")
    fun getState(id: Int = 0): BibleState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveState(state: BibleState)
}
