package com.example.project_pemob_techie.ui.content

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BookViewModel : ViewModel() {
    private val _imagePath = MutableLiveData<String>()
    val imagePath: LiveData<String> get() = _imagePath

    fun setImagePath(path: String) {
        _imagePath.value = path
    }
}
