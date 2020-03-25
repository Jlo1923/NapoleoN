package com.naposystems.pepito.ui.status

import android.content.Context
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

    private lateinit var _status: LiveData<List<Status>>
    val status: LiveData<List<Status>>
        get() = _status

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

    override fun updateStatus(context: Context, textStatus: String) {
        viewModelScope.launch {
            try {

                user.value?.let {user ->
                    val updateUserInfoReqDTO = UpdateUserInfoReqDTO(
                        displayName = user.displayName,
                        status = textStatus
                    )

                    val response = repository.updateRemoteStatus(updateUserInfoReqDTO)

                    if (response.isSuccessful) {
                        status.value?.let { listStatus ->
                            if(listStatus.count() < 10){
                                val status = listStatus.find {
                                    (it.resourceId != 0) && (context.getString(it.resourceId).trim() == updateUserInfoReqDTO.status.trim()) ||
                                    (it.resourceId == 0) && (it.customStatus.trim() == updateUserInfoReqDTO.status.trim())
                                }

                                if (status == null) {
                                    val list = arrayListOf<Status>()
                                    list.add(Status(0, customStatus = updateUserInfoReqDTO.status))
                                    repository.insertNewStatus(list)
                                }
                            }
                        }

                        repository.updateLocalStatus(
                            updateUserInfoReqDTO.status,
                            user.firebaseId
                        )
                        user.status = updateUserInfoReqDTO.status
                    } else {
                        when (response.code()) {
                            401, 500 -> _errorUpdatingStatus.value =
                                repository.getDefaultError(response)
                            422 -> _errorUpdatingStatus.value = repository.get422Error(response)
                            else -> _errorUpdatingStatus.value = repository.getDefaultError(response)
                        }
                    }
                }
            } catch (ex: Exception) {
                Timber.d(ex)
                _errorUpdatingStatus.value = emptyList()
            }
        }
    }

    override fun deleteStatus(status: Status) {
        viewModelScope.launch {
            repository.deleteStatus(status)
        }
    }

    //endregion
}
