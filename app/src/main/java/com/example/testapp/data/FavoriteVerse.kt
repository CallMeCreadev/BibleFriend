package com.example.testapp.data

import androidx.room.Entity

@Entity(tableName = "favorite_verses", primaryKeys = ["chapter", "verse"])
data class FavoriteVerse(
    val chapter: String,
    val verse: String,
    val text: String
)
