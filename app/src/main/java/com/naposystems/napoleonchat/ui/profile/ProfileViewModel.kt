package com.naposystems.napoleonchat.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.entity.User
import com.naposystems.napoleonchat.repository.profile.ProfileRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel(),
    IContractProfile.ViewModel {

    lateinit var user: LiveData<User>

    private val _errorGettingLocalUser = MutableLiveData<Boolean>()
    val errorGettingLocalUser: LiveData<Boolean>
        get() = _errorGettingLocalUser

    init {
        getLocalUser()
        _errorGettingLocalUser.value = false
    }

    //region Implementation IContractProfile.ViewModel
    override fun getLocalUser() {
        viewModelScope.launch {
            try {
                user = repository.getUser()
            } catch (ex: Exception) {
                Timber.e(ex)
                _errorGettingLocalUser.value = true
            }
        }
    }

    override fun updateLocalUser(newUser: User) {
        viewModelScope.launch {
            try {
                repository.updateLocalUser(newUser)
            } catch (ex: Exception) {
                Timber.d(ex)
            }
        }
    }

    override fun getUser() = this.user.value

    //endregion
}