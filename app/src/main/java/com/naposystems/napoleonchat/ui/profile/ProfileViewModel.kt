package com.naposystems.napoleonchat.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.repository.profile.ProfileRepositoryImp
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepositoryImp
) : ViewModel() {

    lateinit var userEntity: LiveData<UserEntity>

    private val _errorGettingLocalUser = MutableLiveData<Boolean>()
    val errorGettingLocalUser: LiveData<Boolean>
        get() = _errorGettingLocalUser

    init {
        getLocalUser()
        _errorGettingLocalUser.value = false
    }

    //region Implementation IContractProfile.ViewModel
    private fun getLocalUser() {
        viewModelScope.launch {
            try {
                userEntity = repository.getUser()
            } catch (ex: Exception) {
                Timber.e(ex)
                _errorGettingLocalUser.value = true
            }
        }
    }

    fun updateLocalUser(newUserEntity: UserEntity) {
        viewModelScope.launch {
            try {
                repository.updateLocalUser(newUserEntity)
            } catch (ex: Exception) {
                Timber.d(ex)
            }
        }
    }

    fun getUser() = this.userEntity.value

    fun disconnectSocket() {
        repository.disconnectSocket()
    }

    //endregion
}
