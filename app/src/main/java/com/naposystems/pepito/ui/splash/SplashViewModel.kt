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
}
