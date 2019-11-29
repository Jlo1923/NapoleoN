package com.naposystems.pepito.ui.landing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LandingViewModel : ViewModel() {
    private val _showLanguageSelection = MutableLiveData<Boolean>()
    val showLanguageSelection: LiveData<Boolean>
        get() = _showLanguageSelection

    private val _openSendCode = MutableLiveData<Boolean>()
    val openSendCode: LiveData<Boolean>
        get() = _openSendCode

    private val _openRecoveryAccount = MutableLiveData<Boolean>()
    val openRecoveryAccount: LiveData<Boolean>
        get() = _openRecoveryAccount

    init {
        _showLanguageSelection.value = false
        _openSendCode.value = false
        _openRecoveryAccount.value = false
    }

    fun onShowLanguageSelectionPressed() {
        _showLanguageSelection.value = true
    }

    fun onLanguageSelectionShowed() {
        _showLanguageSelection.value = null
    }

    fun onRegisterButtonPressed() {
        _openSendCode.value = true
    }

    fun onSendCodeOpened() {
        _openSendCode.value = null
    }

    fun onRecoveryAccountButtonPressed() {
        _openRecoveryAccount.value = true
    }

    fun onRecoveryAccountOpened() {
        _openRecoveryAccount.value = null
    }
}
