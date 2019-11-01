package com.naposystems.pepito.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegisterViewModel : ViewModel() {

    val nickName = MutableLiveData<String>()
    val displayName = MutableLiveData<String>()

    private val _openTermsAndConditions = MutableLiveData<Boolean>()
    val openTermsAndConditions: LiveData<Boolean>
        get() = _openTermsAndConditions

    init {
        nickName.value = ""
        displayName.value = ""
        _openTermsAndConditions.value = false
    }

    fun onTermsAndConditionsPressed() {
        _openTermsAndConditions.value = true
    }

    fun onTermsAndConditionsLaunched() {
        _openTermsAndConditions.value = null
    }
}
