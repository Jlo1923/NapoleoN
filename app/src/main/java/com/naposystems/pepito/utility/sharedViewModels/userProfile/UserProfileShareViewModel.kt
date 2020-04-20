package com.naposystems.pepito.utility.sharedViewModels.userProfile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.repository.sharedRepository.UserProfileShareRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class UserProfileShareViewModel @Inject constructor(
    private val repository: UserProfileShareRepository
) : ViewModel(), IContractUserProfileShare.ViewModel {

    private val _userUpdated = MutableLiveData<Boolean>()
    val userUpdated: LiveData<Boolean>
        get() = _userUpdated

    private lateinit var _user : LiveData<User>
    val user: LiveData<User>
        get() = _user

    private val _errorUpdatingUser = MutableLiveData<List<String>>()
    val errorUpdatingUser: LiveData<List<String>>
        get() = _errorUpdatingUser

    init {
        _userUpdated.value = null
        _errorUpdatingUser.value = emptyList()
    }

    override fun getUser() {
        viewModelScope.launch {
            _user = repository.getUser()
        }
    }

    override fun updateUserInfo(user : User, updateUserInfoReqDTO: Any) {
        viewModelScope.launch {
            try {
                val response = repository.updateUserInfo(updateUserInfoReqDTO)

                if (response.isSuccessful) {
                    response.body()?.let { updatedUser->
                        user.let { userLocal ->
                            val userNew = User(
                                firebaseId = userLocal.firebaseId,
                                id = userLocal.id,
                                nickname = updatedUser.nickname,
                                displayName = updatedUser.displayName,
                                accessPin = userLocal.accessPin,
                                imageUrl = updatedUser.avatarUrl,
                                status = updatedUser.status,
                                headerUri = userLocal.headerUri,
                                chatBackground = userLocal.chatBackground,
                                type = userLocal.type,
                                createAt = userLocal.createAt
                            )
                            updateUserLocal(userNew)
                        }
                    }
                    _userUpdated.value = true
                } else {
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

    override fun updateUserLocal(user: User) {
        viewModelScope.launch {
            repository.updateUserLocal(user)
        }
    }

}