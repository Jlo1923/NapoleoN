package com.naposystems.pepito.ui.landing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LandingViewModel : ViewModel() {
    private val _showLanguageSelection = MutableLiveData<Boolean>()
    val showLanguageSelection: LiveData<Boolean>
        get() = _showLanguageSelection

    init {
        _showLanguageSelection.value = false
    }

    fun onShowLanguageSelectionPressed() {
        _showLanguageSelection.value = true
    }

    fun onLanguageSelectionShowed() {
        _showLanguageSelection.value = null
    }
}
