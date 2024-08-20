package com.example.testapp.ui.question

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class QuestionViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Question Fragment"
    }
    val text: LiveData<String> = _text
}