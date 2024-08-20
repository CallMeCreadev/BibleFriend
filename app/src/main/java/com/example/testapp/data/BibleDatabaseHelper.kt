package com.example.testapp.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class BibleDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    init {
        val dbFile = context.getDatabasePath(DB_NAME)
        if (!dbFile.exists()) {
            try {
                copyDatabase(context)
            } catch (e: IOException) {
                throw RuntimeException("Error copying database", e)
            }
        }
    }

    @Throws(IOException::class)
    private fun copyDatabase(context: Context) {
        val inputStream: InputStream = context.assets.open(DB_NAME)
        val outFileName = context.getDatabasePath(DB_NAME).path
        val outputStream: OutputStream = FileOutputStream(outFileName)
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }
        outputStream.flush()
        outputStream.close()
        inputStream.close()
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // No need to implement this for a pre-made database
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // No need to implement this for a pre-made database
    }

    companion object {
        private const val DB_NAME = "bible.db"
        private const val DB_VERSION = 1
    }
}
