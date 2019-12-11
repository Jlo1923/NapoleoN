package com.naposystems.pepito.ui.timeAccessPin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class TimeAccessPinDialogViewModel @Inject constructor(
    private val repository: IContractTimeAccessPin.Repository
) : ViewModel(), IContractTimeAccessPin.ViewModel {

    private val _timeAccessPin = MutableLiveData<Int>()
    val timeAccessPin: LiveData<Int>
        get() = _timeAccessPin

    //region Implementation IContractTimeAccessPin.ViewModel
    override fun getTimeAccessPin() {
        viewModelScope.launch {
            _timeAccessPin.value = repository.getTimeAccessPin()
        }
    }

    override fun setTimeAccessPin(time: Int) {
        viewModelScope.launch {
            repository.setTimeAccessPin(time)
            _timeAccessPin.value = time
        }
    }
    //endregion
}
