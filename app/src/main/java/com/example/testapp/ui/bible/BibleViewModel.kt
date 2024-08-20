package com.example.testapp.ui.bible

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BibleViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Bible Fragment"
    }
    val text: LiveData<String> = _text
    val pageIndex = MutableLiveData<Int>()
    val isOpenInBibleNavigation = MutableLiveData<Boolean>(false)
}