package com.naposystems.napoleonchat.utility.sharedViewModels.userProfile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.repository.sharedRepository.UserProfileShareRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class UserProfileShareViewModel @Inject constructor(
    private val repository: UserProfileShareRepository
) : ViewModel(), IContractUserProfileShare.ViewModel {

    private val _userUpdated = MutableLiveData<Boolean>()
    val userUpdated: LiveData<Boolean>
        get() = _userUpdated

    private lateinit var _userEntity : LiveData<UserEntity>
    val userEntity: LiveData<UserEntity>
        get() = _userEntity

    private val _errorUpdatingUser = MutableLiveData<List<String>>()
    val errorUpdatingUser: LiveData<List<String>>
        get() = _errorUpdatingUser

    init {
        _userUpdated.value = null
        _errorUpdatingUser.value = emptyList()
    }

    override fun getUser() {
        viewModelScope.launch {
            _userEntity = repository.getUser()
        }
    }

    override fun updateUserInfo(userEntity : UserEntity, updateUserInfoReqDTO: Any) {
        viewModelScope.launch {
            try {
                val response = repository.updateUserInfo(updateUserInfoReqDTO)

                if (response.isSuccessful) {
                    response.body()?.let { updatedUser->
                        userEntity.let { userLocal ->
                            val userNew = UserEntity(
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

    override fun updateUserLocal(userEntity: UserEntity) {
        viewModelScope.launch {
            repository.updateUserLocal(userEntity)
        }
    }

}