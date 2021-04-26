package com.naposystems.napoleonchat.utility.sharedViewModels.userProfile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.utility.Constants
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class UserProfileSharedViewModel @Inject constructor(
    private val repository: UserProfileSharedRepositoryImp
) : ViewModel() {

    private val _userUpdated = MutableLiveData<Boolean>()
    val userUpdated: LiveData<Boolean>
        get() = _userUpdated

    private lateinit var _userEntity: LiveData<UserEntity>
    val userEntity: LiveData<UserEntity>
        get() = _userEntity

    private val _errorUpdatingUser = MutableLiveData<List<String>>()
    val errorUpdatingUser: LiveData<List<String>>
        get() = _errorUpdatingUser

    init {
        _userUpdated.value = null
        _errorUpdatingUser.value = emptyList()
    }

    fun getUser() {
        viewModelScope.launch {
            _userEntity = repository.getUser()
        }
    }

    fun updateUserInfo(userEntity: UserEntity, updateUserInfoReqDTO: Any) {
        viewModelScope.launch {
            try {
                val response = repository.updateUserInfo(updateUserInfoReqDTO)

                if (response.isSuccessful) {
                    response.body()?.let { updatedUser ->
                        //TODO: Implementar Extension Function
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
                        Constants.CodeHttp.UNAUTHORIZED.code,
                        Constants.CodeHttp.INTERNAL_SERVER_ERROR.code -> _errorUpdatingUser.value =
                            repository.getDefaultError(response)
                        Constants.CodeHttp.UNPROCESSABLE_ENTITY.code -> _errorUpdatingUser.value =
                            repository.getUnprocessableEntityError(response)
                        else -> _errorUpdatingUser.value = repository.getDefaultError(response)
                    }
                }
            } catch (ex: Exception) {
                Timber.d(ex)
                _errorUpdatingUser.value = emptyList()
            }
        }
    }

    fun updateUserLocal(userEntity: UserEntity) {
        viewModelScope.launch {
            repository.updateUserLocal(userEntity)
        }
    }

}