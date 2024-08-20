package com.example.testapp.data

sealed class BibleItem {
    data class ChapterTitle(val title: String) : BibleItem()
    data class Verse(val verseNumber: Int, val verse: String) : BibleItem()
}
