package com.naposystems.napoleonchat.ui.status

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.repository.status.StatusRepository
import com.naposystems.napoleonchat.source.local.entity.StatusEntity
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.source.remote.dto.status.UserStatusReqDTO
import com.naposystems.napoleonchat.utility.Constants
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class StatusViewModel
@Inject constructor(
    private val repository: StatusRepository
) : ViewModel() {

    val user = MutableLiveData<UserEntity>()

    private lateinit var _statusEntity: LiveData<MutableList<StatusEntity>>
    val statusEntity: LiveData<MutableList<StatusEntity>>
        get() = _statusEntity

    private val _statusUpdatedSuccessfully = MutableLiveData<Boolean>()
    val statusUpdatedSuccessfully: LiveData<Boolean>
        get() = _statusUpdatedSuccessfully

    private val _errorGettingStatus = MutableLiveData<Boolean>()
    val errorGettingStatus: LiveData<Boolean>
        get() = _errorGettingStatus

    private val _errorUpdatingStatus = MutableLiveData<List<String>>()
    val errorUpdatingStatus: LiveData<List<String>>
        get() = _errorUpdatingStatus

    init {
        _errorGettingStatus.value = false
        _errorUpdatingStatus.value = emptyList()
        getStatus()
    }

    //region Implementation IContractStatus.ViewModel
    fun getStatus() {
        viewModelScope.launch {
            try {
                _statusEntity = repository.getStatus()
            } catch (ex: Exception) {
                _errorGettingStatus.value = true
                Timber.d(ex)
            }
        }
    }

    fun updateStatus(textStatus: String) {
        viewModelScope.launch {
            try {
                user.value?.let { user ->
                    val userStatus = UserStatusReqDTO(
                        status = textStatus
                    )
                    val response = repository.updateRemoteStatus(userStatus)

                    if (response.isSuccessful) {
                        handleUpdateRemoteStatusSuccessful(textStatus, user)
                        _statusUpdatedSuccessfully.value = true
                    } else {
                        when (response.code()) {
                            Constants.CodeHttp.UNPROCESSABLE_ENTITY.code -> _errorUpdatingStatus.value =
                                repository.getUnprocessableEntityError(response)
                            else -> _errorUpdatingStatus.value =
                                repository.getDefaultError(response)
                        }
                    }
                }
            } catch (ex: Exception) {
                Timber.d(ex)
                _errorUpdatingStatus.value = emptyList()
            }
        }
    }

    fun insertStatus(listStatusEntities: List<StatusEntity>) {
        viewModelScope.launch {
            repository.insertNewStatus(listStatusEntities)
        }
    }

    private suspend fun handleUpdateRemoteStatusSuccessful(
        textStatus: String,
        userEntity: UserEntity
    ) {
        statusEntity.value?.let { listStatus ->
            if (listStatus.count() < 10) {
                val status = listStatus.find {
                    (it.status.isNotEmpty()) && (it.status
                        .trim() == textStatus.trim()) ||
                            (it.status.isEmpty()) && (it.customStatus.trim() == textStatus.trim())
                }

                if (status == null) {
                    val list = arrayListOf<StatusEntity>()
                    list.add(StatusEntity(0, customStatus = textStatus))
                    repository.insertNewStatus(list)
                }
            }
        }

        repository.updateLocalStatus(
            textStatus,
            userEntity.firebaseId
        )
        userEntity.status = textStatus
    }

    fun deleteStatus(statusEntity: StatusEntity) {
        viewModelScope.launch {
            repository.deleteStatus(statusEntity)
        }
    }

    //endregion
}
