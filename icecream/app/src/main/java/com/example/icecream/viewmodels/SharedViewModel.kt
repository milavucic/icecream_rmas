package com.example.icecream.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.icecream.data.Icecream

class SharedViewModel : ViewModel() {
    private val _icecreams = MutableLiveData<List<Icecream>>()
    val icecreams: LiveData<List<Icecream>> get() = _icecreams

    fun setIcecreams(list: Any?) {
        _icecreams.value = list as List<Icecream>?
    }
}
