package com.naposystems.pepito.ui.status

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.dto.profile.UpdateUserInfoReqDTO
import com.naposystems.pepito.entity.Status
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.repository.status.StatusRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.Exception

class StatusViewModel @Inject constructor(private val repository: StatusRepository) :
    ViewModel(),
    IContractStatus.ViewModel {

    val user = MutableLiveData<User>()

    private val _status = MutableLiveData<List<Status>>()
    val status: LiveData<List<Status>>
        get() = _status

    private val _errorGettingStatus = MutableLiveData<Boolean>()
    val errorGettingStatus: LiveData<Boolean>
        get() = _errorGettingStatus

    private val _errorUpdatingStatus = MutableLiveData<List<String>>()
    val errorUpdatingStatus: LiveData<List<String>>
        get() = _errorUpdatingStatus

    init {
        _status.value = emptyList()
        _errorGettingStatus.value = false
        _errorUpdatingStatus.value = emptyList()
        getStatus()
    }

    //region Implementation IContractStatus.ViewModel
    override fun getStatus() {
        viewModelScope.launch {
            try {
                _status.value = repository.getStatus()
            } catch (ex: Exception) {
                _errorGettingStatus.value = true
                Timber.d(ex)
            }
        }
    }

    override fun updateStatus(updateUserInfoReqDTO: UpdateUserInfoReqDTO) {
        viewModelScope.launch {
            try {
                val response = repository.updateRemoteStatus(updateUserInfoReqDTO)

                if (response.isSuccessful) {
                    repository.updateLocalStatus(
                        updateUserInfoReqDTO.status,
                        user.value!!.firebaseId
                    )

                    user.value!!.status = updateUserInfoReqDTO.status
                } else {
                    when (response.code()) {
                        401, 500 -> _errorUpdatingStatus.value =
                            repository.getDefaultError(response)
                        422 -> _errorUpdatingStatus.value = repository.get422Error(response)
                        else -> _errorUpdatingStatus.value = repository.getDefaultError(response)
                    }
                }
            } catch (ex: Exception) {
                Timber.d(ex)
                _errorUpdatingStatus.value = emptyList()
            }
        }
    }

    //endregion
}
