package com.example.testapp.ui.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _text = MutableLiveData<String>().apply {
        value = ""
    }
    val text: LiveData<String> = _text

    private val _fontSize = MutableLiveData<Float>().apply {
        value = getFontSizeFromPreferences()
    }
    val fontSize: LiveData<Float> = _fontSize

    fun setFontSize(size: Float) {
        _fontSize.value = size
        saveFontSizeToPreferences(size)
    }

    private fun saveFontSizeToPreferences(size: Float) {
        val sharedPreferences = getApplication<Application>().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putFloat("font_size", size)
            apply()
        }
    }

    private fun getFontSizeFromPreferences(): Float {
        val sharedPreferences = getApplication<Application>().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getFloat("font_size", 20f) // Default to 20sp
    }
}
