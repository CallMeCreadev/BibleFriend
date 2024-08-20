package com.example.testapp.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase

class BibleRepository(private val context: Context) {

    private val dbName = "bible.db"
    private val dbPath = context.getDatabasePath(dbName).absolutePath

    fun getVersesByPage(page: Int, versesPerPage: Int): List<BibleItem> {
        val items = mutableListOf<BibleItem>()
        val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)

        val offset = page * versesPerPage
        val cursor = db.rawQuery(
            "SELECT bible_book, Verse_Number, Verse FROM Bible ORDER BY rowid LIMIT ? OFFSET ?",
            arrayOf(versesPerPage.toString(), offset.toString())
        )

        var previousChapter: String? = null

        if (cursor.moveToFirst()) {
            do {
                val book = cursor.getString(cursor.getColumnIndexOrThrow("bible_book"))
                val verseNumber = cursor.getInt(cursor.getColumnIndexOrThrow("Verse_Number"))
                val verse = cursor.getString(cursor.getColumnIndexOrThrow("Verse"))

                if (book != previousChapter) {
                    items.add(BibleItem.ChapterTitle(book))
                    previousChapter = book
                }
                items.add(BibleItem.Verse(verseNumber, verse))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return items
    }

    fun getVerseCount(): Int {
        val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)
        val cursor = db.rawQuery("SELECT COUNT(*) FROM Bible", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }

    fun getBibleState(): BibleState? {
        // Retrieve and return the saved state from persistent storage (e.g., SharedPreferences)
        return null
    }

    fun saveBibleState(state: BibleState) {
        // Save the state to persistent storage (e.g., SharedPreferences)
    }
}
