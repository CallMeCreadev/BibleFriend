package com.example.testapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BibleVerseDao {
    @Insert
    fun insertAll(verses: List<Bible>)

    @Query("SELECT * FROM bible LIMIT :limit OFFSET :offset")
    fun getVerses(limit: Int, offset: Int): List<Bible>

    @Query("SELECT COUNT(*) FROM bible")
    fun getVerseCount(): Int


}
