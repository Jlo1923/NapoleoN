package com.naposystems.napoleonchat.ui.activateBiometrics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ActivateBiometricsViewModel @Inject constructor(
    private val repository: IContractActivateBiometrics.Repository
) : ViewModel(), IContractActivateBiometrics.ViewModel {

    private val _biometricsOption = MutableLiveData<Int>()
    val biometricsOption: LiveData<Int>
        get() = _biometricsOption

    override fun getBiometricsOption() {
        viewModelScope.launch {
            _biometricsOption.value = repository.getBiometricsOption()
        }
    }

    override fun setBiometricsOption(option: Int) {
        viewModelScope.launch {
            repository.setBiometricsOption(option)
            _biometricsOption.value = option
        }
    }
}
