package com.naposystems.pepito.ui.securitySettings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class SecuritySettingsViewModel @Inject constructor(
    private val repository: IContractSecuritySettings.Repository
) : ViewModel(), IContractSecuritySettings.ViewModel {

    private val _selfDestructTime = MutableLiveData<Int>()
    val selfDestructTime: LiveData<Int>
        get() = _selfDestructTime

    private val _timeRequestAccessPin = MutableLiveData<Int>()
    val timeRequestAccessPin: LiveData<Int>
        get() = _timeRequestAccessPin

    //region Implementation IContractSecuritySettings.ViewModel
    override fun getSelfDestructTime() {
        _selfDestructTime.value = repository.getSelfDestructTime()
    }

    override fun getTimeRequestAccessPin() {
        _timeRequestAccessPin.value = repository.getTimeRequestAccessPin()
    }

    //endregion
}
