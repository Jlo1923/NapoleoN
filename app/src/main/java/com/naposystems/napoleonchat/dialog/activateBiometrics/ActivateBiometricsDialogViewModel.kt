package com.naposystems.napoleonchat.dialog.activateBiometrics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ActivateBiometricsDialogViewModel @Inject constructor(
    private val repository: ActivateBiometricsDialogRepository
) : ViewModel() {

    private val _biometricsOption = MutableLiveData<Int>()
    val biometricsOption: LiveData<Int>
        get() = _biometricsOption

    fun getBiometricsOption() {
        viewModelScope.launch {
            _biometricsOption.value = repository.getBiometricsOption()
        }
    }

    fun setBiometricsOption(option: Int) {
        viewModelScope.launch {
            repository.setBiometricsOption(option)
            _biometricsOption.value = option
        }
    }
}
