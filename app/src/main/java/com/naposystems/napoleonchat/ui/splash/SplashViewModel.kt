package com.naposystems.napoleonchat.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.repository.splash.SplashRepository
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import kotlinx.coroutines.launch
import javax.inject.Inject

class SplashViewModel @Inject constructor(
    private val repository: SplashRepository
) : ViewModel() {

    private val _navigateToLanding = MutableLiveData<Boolean>()
    val navigateToLanding: LiveData<Boolean>
        get() = _navigateToLanding

    private val _user = MutableLiveData<UserEntity>()
    val userEntity: LiveData<UserEntity>
        get() = _user

    private val _typeLock = MutableLiveData<Int>()
    val typeLock: LiveData<Int>
        get() = _typeLock

    private val _timeAccessPin = MutableLiveData<Int>()
    val timeAccessPin: LiveData<Int>
        get() = _timeAccessPin

    private val _lockTimeApp = MutableLiveData<Long>()
    val lockTimeApp: LiveData<Long>
        get() = _lockTimeApp

    private val _lockStatus = MutableLiveData<Int>()
    val lockStatus: LiveData<Int>
        get() = _lockStatus

    private val _unlockTimeApp = MutableLiveData<Long>()
    val unlockTimeApp: LiveData<Long>
        get() = _unlockTimeApp

    private val _accountStatus = MutableLiveData<Int>()
    val accountStatus: LiveData<Int>
        get() = _accountStatus

    fun onLoadingTimeEnd() {
        _navigateToLanding.value = true
    }

    fun doneNavigateToLanding() {
        _navigateToLanding.value = null
    }

    fun getUser() {
        viewModelScope.launch {
            _user.value = repository.getUser()
        }
    }

    fun getTimeRequestAccessPin() {
        viewModelScope.launch {
            _timeAccessPin.value = repository.getTimeRequestAccessPin()
        }
    }

    fun getLockTime() {
        viewModelScope.launch {
            _lockTimeApp.value = repository.getLockTime()
        }
    }

    fun getLockStatus() {
        viewModelScope.launch {
            _lockStatus.value = repository.getLockStatus()
        }
    }

    fun getLockType() {
        viewModelScope.launch {
            _typeLock.value = repository.getLockType()
        }
    }

    fun getUnlockTimeApp() {
        viewModelScope.launch {
            _unlockTimeApp.value = repository.getUnlockTimeApp()
        }
    }

    fun getAccountStatus() {
        viewModelScope.launch {
            _accountStatus.value = repository.getAccountStatus()
        }
    }


    fun setDefaultLanguage(language: String) {
        viewModelScope.launch {
            repository.setDefaultLanguage(language)
        }
    }

    fun setDefaultBiometricsOption(biometricOption: Int) {
        viewModelScope.launch {
            repository.setDefaultBiometricsOption(biometricOption)
        }
    }

    fun clearData() {
        viewModelScope.launch {
            repository.clearData()
        }
    }
}
