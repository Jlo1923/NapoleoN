package com.naposystems.pepito.ui.landing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LandingViewModel : ViewModel() {
    private val _showLanguageSelection = MutableLiveData<Boolean>()
    val showLanguageSelection: LiveData<Boolean>
        get() = _showLanguageSelection

    private val _openRegister = MutableLiveData<Boolean>()
    val openRegister: LiveData<Boolean>
        get() = _openRegister

    init {
        _showLanguageSelection.value = false
        _openRegister.value = false
    }

    fun onShowLanguageSelectionPressed() {
        _showLanguageSelection.value = true
    }

    fun onLanguageSelectionShowed() {
        _showLanguageSelection.value = null
    }

    fun onRegisterButtonPressed() {
        _openRegister.value = true
    }

    fun onRegisterOpened() {
        _openRegister.value = null
    }
}
