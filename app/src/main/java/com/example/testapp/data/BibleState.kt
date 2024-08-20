package com.example.testapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bible_state")
data class BibleState(
    @PrimaryKey val id: Int = 0,
    val pageIndex: Int,
    val scrollPosition: Int
)
