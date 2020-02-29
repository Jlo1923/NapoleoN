package com.naposystems.pepito.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.dto.profile.UpdateUserInfoReqDTO
import com.naposystems.pepito.dto.profile.UpdateUserInfoResDTO
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.repository.profile.ProfileRepository
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val sharedPreferencesManager: SharedPreferencesManager
) : ViewModel(),
    IContractProfile.ViewModel {

    val user = MutableLiveData<User>()

    private val _errorGettingLocalUser = MutableLiveData<Boolean>()
    val errorGettingLocalUser: LiveData<Boolean>
        get() = _errorGettingLocalUser

    private val _userUpdated = MutableLiveData<UpdateUserInfoResDTO>()
    val userUpdated: LiveData<UpdateUserInfoResDTO>
        get() = _userUpdated

    private val _errorUpdatingUser = MutableLiveData<List<String>>()
    val errorUpdatingUser: LiveData<List<String>>
        get() = _errorUpdatingUser

    private val _localUserUpdated = MutableLiveData<Boolean>()
    val localUserUpdated: LiveData<Boolean>
        get() = _localUserUpdated

    init {
        getUser()
        _errorGettingLocalUser.value = false
        _userUpdated.value = null
        _errorUpdatingUser.value = emptyList()
        _localUserUpdated.value = null
    }

    private fun updateUserInfo(
        updateUserInfoReqDTO: UpdateUserInfoReqDTO,
        successCallback: () -> Unit,
        failureCallback: () -> Unit
    ) {
        viewModelScope.launch {
            try {

                val response = repository.updateUserInfo(updateUserInfoReqDTO)

                if (response.isSuccessful) {
                    _userUpdated.value = response.body()
                    successCallback()
                } else {
                    failureCallback()
                    when (response.code()) {
                        401, 500 -> _errorUpdatingUser.value = repository.getDefaultError(response)
                        422 -> _errorUpdatingUser.value = repository.get422Error(response)
                        else -> _errorUpdatingUser.value = repository.getDefaultError(response)
                    }
                }
            } catch (ex: Exception) {
                Timber.d(ex)
                _errorUpdatingUser.value = emptyList()
            }
        }
    }

    //region Implementation IContractProfile.ViewModel
    override fun getUser() {
        viewModelScope.launch {
            try {
                user.value = repository.getUser()
            } catch (ex: Exception) {
                Timber.e(ex)
                _errorGettingLocalUser.value = true
            }
        }
    }

    override fun updateAvatar(updateUserInfoReqDTO: UpdateUserInfoReqDTO) =
        updateUserInfo(updateUserInfoReqDTO, {}, {})

    override fun updateDisplayName(
        updateUserInfoReqDTO: UpdateUserInfoReqDTO,
        successCallback: () -> Unit,
        failureCallback: () -> Unit
    ) {
        updateUserInfo(updateUserInfoReqDTO, successCallback, failureCallback)
    }

    override fun updateLocalUser(newUser: User) {
        viewModelScope.launch {
            try {
                repository.updateLocalUser(newUser)
                user.value = newUser
            } catch (ex: Exception) {
                Timber.d(ex)
            }
        }
    }

    //endregion
}
