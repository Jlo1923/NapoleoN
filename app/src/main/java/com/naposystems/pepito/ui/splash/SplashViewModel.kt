package com.naposystems.pepito.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.entity.User
import kotlinx.coroutines.launch
import javax.inject.Inject

class SplashViewModel @Inject constructor(
    private val repository: IContractSplash.Repository
) : ViewModel(), IContractSplash.ViewModel {

    private val _navigateToLanding = MutableLiveData<Boolean>()
    val navigateToLanding: LiveData<Boolean>
        get() = _navigateToLanding

    private val _user = MutableLiveData<User>()
    val user: LiveData<User>
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

    override fun getUser() {
        viewModelScope.launch {
            _user.value = repository.getUser()
        }
    }

    override fun getTimeRequestAccessPin() {
        viewModelScope.launch {
            _timeAccessPin.value = repository.getTimeRequestAccessPin()
        }
    }

    override fun getLockTime() {
        viewModelScope.launch {
            _lockTimeApp.value = repository.getLockTime()
        }
    }

    override fun getLockStatus() {
        viewModelScope.launch {
            _lockStatus.value = repository.getLockStatus()
        }
    }

    override fun getLockType() {
        viewModelScope.launch {
            _typeLock.value = repository.getLockType()
        }
    }

    override fun getUnlockTimeApp() {
        viewModelScope.launch {
            _unlockTimeApp.value = repository.getUnlockTimeApp()
        }
    }

    override fun getAccountStatus() {
        viewModelScope.launch {
            _accountStatus.value = repository.getAccountStatus()
        }
    }

    override fun setDefaultPreferences() {
        viewModelScope.launch {
            repository.setDefaultTheme()
            repository.setDefaultUserDisplayFormat()
            repository.setDefaultTimeFormat()
            repository.setDefaultSelfDestructTime()
            repository.setDefaultTimeRequestAccessPin()
            repository.setDefaultAllowDownloadAttachments()
            repository.setDefaultLockType()
            repository.setDefaultSelfDestructTimeMessageNotSent()
            repository.setDefaultAttemptsForRetryCode()
            repository.setDefaultTimeForRetryCode()
            repository.setDefaultAttemptsForNewCode()
        }
    }

    override fun setDefaultLanguage(language: String) {
        viewModelScope.launch {
            repository.setDefaultLanguage(language)
        }
    }

    override fun setDefaultBiometricsOption(biometricOption: Int) {
        viewModelScope.launch {
            repository.setDefaultBiometricsOption(biometricOption)
        }
    }
}
