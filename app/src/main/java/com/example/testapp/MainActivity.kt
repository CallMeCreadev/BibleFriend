package com.example.testapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.testapp.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivityCheckForCrashAlertOnly"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Start")

        try {
            copyDatabaseIfNeeded(this)
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            val navView: BottomNavigationView = binding.navView

            val navController = findNavController(R.id.nav_host_fragment_activity_main)
            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.navigation_home, R.id.navigation_bible, R.id.navigation_favorites, R.id.navigation_question
                )
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)

            navView.setOnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_home -> {
                        navController.navigate(R.id.navigation_home)
                        Log.d(TAG, "Navigation: Home selected")
                        true
                    }
                    R.id.navigation_bible -> {
                        if (navController.currentDestination?.id != R.id.navigation_bible) {
                            navController.navigate(R.id.navigation_bible)
                        }
                        Log.d(TAG, "Navigation: Bible selected")
                        true
                    }
                    R.id.navigation_favorites -> {
                        if (navController.currentDestination?.id != R.id.navigation_favorites) {
                            navController.navigate(R.id.navigation_favorites)
                        }
                        Log.d(TAG, "Navigation: Favorites selected")
                        true
                    }
                    R.id.navigation_question -> {
                        if (navController.currentDestination?.id != R.id.navigation_question) {
                            navController.navigate(R.id.navigation_question)
                        }
                        Log.d(TAG, "Navigation: Question selected")
                        true
                    }
                    else -> false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: Exception occurred", e)
        }

        Log.d(TAG, "onCreate: End")
    }


    private fun copyDatabaseIfNeeded(context: Context) {
        Log.d(TAG, "copyDatabaseIfNeeded: Start")
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isDbCopied = sharedPreferences.getBoolean("db_copied", false)
        Log.d(TAG, "copyDatabaseIfNeeded: isDbCopied = $isDbCopied")

        if (!isDbCopied) {
            try {
                copyDatabase(context)
                sharedPreferences.edit().putBoolean("db_copied", true).apply()
                Log.d(TAG, "copyDatabaseIfNeeded: Database copied successfully")
            } catch (e: Exception) {
                Log.e(TAG, "copyDatabaseIfNeeded: Error copying database", e)
            }
        } else {
            Log.d(TAG, "copyDatabaseIfNeeded: Database already copied")
        }
        Log.d(TAG, "copyDatabaseIfNeeded: End")
    }

    private fun copyDatabase(context: Context) {
        Log.d(TAG, "copyDatabase: Start")
        val dbName = "bible.db"
        val dbPath = context.getDatabasePath(dbName).absolutePath
        Log.d(TAG, "copyDatabase: dbPath = $dbPath")

        val dbFile = File(dbPath)
        if (dbFile.exists()) {
            Log.d(TAG, "copyDatabase: Database file exists, deleting")
            dbFile.delete()  // Delete the existing database file if it exists
        }

        val assetManager = context.assets

        try {
            val inputStream: InputStream = assetManager.open(dbName)
            val outputStream: OutputStream = FileOutputStream(dbPath)

            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()
            Log.d(TAG, "copyDatabase: Database copied successfully")
        } catch (e: IOException) {
            Log.e(TAG, "copyDatabase: IOException occurred", e)
        } catch (e: Exception) {
            Log.e(TAG, "copyDatabase: Exception occurred", e)
        }
        Log.d(TAG, "copyDatabase: End")
    }
}
