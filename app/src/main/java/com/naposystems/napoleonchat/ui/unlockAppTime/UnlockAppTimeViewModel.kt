package com.naposystems.napoleonchat.ui.unlockAppTime

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.repository.unlockAppTime.UnlockAppTimeRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class UnlockAppTimeViewModel
@Inject constructor(
    private val repository: UnlockAppTimeRepository
) : ViewModel() {

    private val _unlockTimeApp = MutableLiveData<Long>()
    val unlockTimeApp: LiveData<Long>
        get() = _unlockTimeApp

    fun getUnlockTime() {
        viewModelScope.launch {
            _unlockTimeApp.value = repository.getUnlockTime()
        }
    }

    fun setAttempts(attempts: Int) {
        viewModelScope.launch {
            repository.setAttempts(attempts)
        }
    }

    fun setLockType(type: Int) {
        viewModelScope.launch {
            repository.setLockType(type)
        }
    }
}
