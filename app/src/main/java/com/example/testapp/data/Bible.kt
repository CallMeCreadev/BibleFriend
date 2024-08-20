package com.example.testapp.data

import androidx.room.Entity

@Entity(tableName = "bible", primaryKeys = ["chapter", "verse"])
data class Bible(
    val chapter: String,
    val verse: String,
    val text: String,
)
