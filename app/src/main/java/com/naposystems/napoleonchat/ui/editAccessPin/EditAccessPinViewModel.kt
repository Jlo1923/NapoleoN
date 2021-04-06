package com.naposystems.napoleonchat.ui.editAccessPin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import kotlinx.coroutines.launch
import javax.inject.Inject

class EditAccessPinViewModel @Inject constructor(
    private val repository: IContractEditAccessPin.Repository
) : ViewModel(), IContractEditAccessPin.ViewModel {

    val oldAccessPin = MutableLiveData<String>()
    val newAccessPin = MutableLiveData<String>()
    val confirmAccessPin = MutableLiveData<String>()
    private lateinit var userEntity: UserEntity

    private val _accessPinUpdatedSuccessfully = MutableLiveData<Boolean>()
    val accessPinUpdatedSuccessfully: LiveData<Boolean>
        get() = _accessPinUpdatedSuccessfully

    init {
        oldAccessPin.value = ""
        newAccessPin.value = ""
        confirmAccessPin.value = ""
        _accessPinUpdatedSuccessfully.value = null
        getLocalUser()
    }

    //region Implementation IContractEditAccessPin.ViewModel
    override fun getLocalUser() {
        viewModelScope.launch {
            userEntity = repository.getLocalUser()
        }
    }

    override fun validateAccessPin(newAccessPin: String) =
        userEntity.accessPin == newAccessPin

    override fun updateAccessPin(newAccessPin: String) {
        viewModelScope.launch {
            repository.updateAccessPin(newAccessPin, userEntity.firebaseId)
            userEntity = repository.getLocalUser()
            _accessPinUpdatedSuccessfully.value = true
        }
    }
    //endregion
}
