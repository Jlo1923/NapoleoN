package com.naposystems.napoleonchat.ui.status

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.dto.status.UserStatusReqDTO
import com.naposystems.napoleonchat.entity.Status
import com.naposystems.napoleonchat.entity.User
import com.naposystems.napoleonchat.repository.status.StatusRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class StatusViewModel @Inject constructor(private val repository: StatusRepository) :
    ViewModel(),
    IContractStatus.ViewModel {

    val user = MutableLiveData<User>()

    private lateinit var _status: LiveData<MutableList<Status>>
    val status: LiveData<MutableList<Status>>
        get() = _status

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
    override fun getStatus() {
        viewModelScope.launch {
            try {
                _status = repository.getStatus()
            } catch (ex: Exception) {
                _errorGettingStatus.value = true
                Timber.d(ex)
            }
        }
    }

    override fun updateStatus(textStatus: String) {
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
                            422 -> _errorUpdatingStatus.value = repository.get422Error(response)
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

    override fun insertStatus(listStatus: List<Status>) {
        viewModelScope.launch {
            repository.insertNewStatus(listStatus)
        }
    }

    private suspend fun handleUpdateRemoteStatusSuccessful(
        textStatus: String,
        user: User
    ) {
        status.value?.let { listStatus ->
            if (listStatus.count() < 10) {
                val status = listStatus.find {
                    (it.status.isNotEmpty()) && (it.status
                        .trim() == textStatus.trim()) ||
                            (it.status.isEmpty()) && (it.customStatus.trim() == textStatus.trim())
                }

                if (status == null) {
                    val list = arrayListOf<Status>()
                    list.add(Status(0, customStatus = textStatus))
                    repository.insertNewStatus(list)
                }
            }
        }

        repository.updateLocalStatus(
            textStatus,
            user.firebaseId
        )
        user.status = textStatus
    }

    override fun deleteStatus(status: Status) {
        viewModelScope.launch {
            repository.deleteStatus(status)
        }
    }

    //endregion
}
