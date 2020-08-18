package com.naposystems.napoleonchat.ui.unlockAppTime

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class UnlockAppTimeViewModel @Inject constructor(
    private val repository: IContractUnlockAppTime.Repository
) : ViewModel(), IContractUnlockAppTime.ViewModel {

    private val _unlockTimeApp = MutableLiveData<Long>()
    val unlockTimeApp: LiveData<Long>
        get() = _unlockTimeApp

    override fun getUnlockTime() {
        viewModelScope.launch {
            _unlockTimeApp.value = repository.getUnlockTime()
        }
    }

    override fun setAttempts(attempts: Int) {
        viewModelScope.launch {
            repository.setAttempts(attempts)
        }
    }

    override fun setLockType(type: Int) {
        viewModelScope.launch {
            repository.setLockType(type)
        }
    }
}
