package com.example.testapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FavoriteVerseDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(favoriteVerse: FavoriteVerse)

    @Query("SELECT * FROM favorite_verses WHERE chapter = :chapter AND verse = :verse LIMIT 1")
    fun findByChapterAndVerse(chapter: String, verse: String): FavoriteVerse?

    @Query("SELECT * FROM favorite_verses")
    fun getAllFavorites(): List<FavoriteVerse>

    @Query("DELETE FROM favorite_verses WHERE chapter = :chapter AND verse = :verse")
    fun delete(chapter: String, verse: String)
}
