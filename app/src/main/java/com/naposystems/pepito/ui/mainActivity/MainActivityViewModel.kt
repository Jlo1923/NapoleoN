package com.naposystems.pepito.ui.mainActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.repository.mainActivity.MainActivityRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(private val repository: MainActivityRepository) :
    ViewModel(), IContractMainActivity.ViewModel {

    private val _user = MutableLiveData<User>()
    val user: LiveData<User>
        get() = _user

    private val _errorGettingUser = MutableLiveData<Boolean>()
    val errorGettingUser: LiveData<Boolean>
        get() = _errorGettingUser

    private val _theme = MutableLiveData<Int>()
    val theme: LiveData<Int>
        get() = _theme

    private val _accountStatus = MutableLiveData<Int>()
    val accountStatus: LiveData<Int>
        get() = _accountStatus

    private val _timeAccessPin = MutableLiveData<Int>()
    val timeAccessPin: LiveData<Int>
        get() = _timeAccessPin

    init {
        _user.value = null
        _errorGettingUser.value = false
    }

    //region Implementation IContractMainActivity.ViewModel
    override fun getUser() {
        viewModelScope.launch {
            try {
                val localUser = repository.getUser()
                _user.value = localUser
            } catch (ex: Exception) {
                Timber.e(ex)
                _errorGettingUser.value = true
            }
        }
    }

    override fun getTheme() {
        viewModelScope.launch {
            _theme.value = repository.getTheme()
        }
    }

    override fun getAccountStatus() {
        viewModelScope.launch {
            _accountStatus.value = repository.getAccountStatus()
        }
    }

    override fun getTimeRequestAccessPin() {
        viewModelScope.launch {
            _timeAccessPin.value = repository.getTimeRequestAccessPin()
        }
    }

    override fun setLockTimeApp() {
        viewModelScope.launch {
            val timeRequestAccessPin = repository.getTimeRequestAccessPin()
            val currentTime = System.currentTimeMillis()
            val blockTime = currentTime.plus(timeRequestAccessPin)
            repository.setLockTimeApp(blockTime)
        }
    }

    override fun setLockStatus(state: Int) {
        viewModelScope.launch {
            repository.setLockStatus(state)
        }
    }

    override fun getLockTimeApp(): Long {
        var lockTime = 0L
        viewModelScope.launch {
            lockTime = repository.getLockTimeApp()
        }
        return lockTime
    }
}