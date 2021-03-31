package com.naposystems.napoleonchat.ui.baseFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class BaseViewModel @Inject constructor(
    private val repository: IContractBase.Repository
) : ViewModel(), IContractBase.ViewModel {

    private val _outputControl = MutableLiveData<Int>()
    val outputControl: LiveData<Int>
        get() = _outputControl

    override fun outputControl(state: Int) {
        viewModelScope.launch {
            repository.outputControl(state)
        }
    }

    override fun getOutputControl() {
        viewModelScope.launch {
            _outputControl.value = repository.getOutputControl()
        }
    }

    override fun connectSocket() {
        repository.connectSocket()
    }
}
