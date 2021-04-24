package com.naposystems.napoleonchat.ui.baseFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.repository.base.BaseRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class BaseViewModel @Inject constructor(
    private val repository: BaseRepository
) : ViewModel() {

    private val _outputControl = MutableLiveData<Int>()
    val outputControl: LiveData<Int>
        get() = _outputControl

    fun outputControl(state: Int) {
        viewModelScope.launch {
            repository.outputControl(state)
        }
    }

    fun getOutputControl() {
        viewModelScope.launch {
            _outputControl.value = repository.getOutputControl()
        }
    }

    fun connectSocket() {
        repository.connectSocket()
    }
}
