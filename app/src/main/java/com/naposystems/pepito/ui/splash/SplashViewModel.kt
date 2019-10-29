package com.naposystems.pepito.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SplashViewModel : ViewModel() {
    private val _navigateToLanding = MutableLiveData<Boolean>()
    val navigateToLanding: LiveData<Boolean>
        get() = _navigateToLanding

    fun onLoadingTimeEnd() {
        _navigateToLanding.value = true
    }

    fun doneNavigateToLanding() {
        _navigateToLanding.value = null
    }
}
