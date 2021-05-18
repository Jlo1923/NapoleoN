package com.naposystems.napoleonchat.ui.register.accessPin

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.repository.accessPin.AccessPinRepository
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.source.remote.dto.accessPin.CreateAccountReqDTO
import com.naposystems.napoleonchat.source.remote.dto.accessPin.CreateAccountResDTO
import com.naposystems.napoleonchat.utility.Constants
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class AccessPinViewModel @Inject constructor(
    private val context: Context,
    private val repository: AccessPinRepository
) : ViewModel() {

    val accessPin = MutableLiveData<String>()

    val confirmAccessPin = MutableLiveData<String>()

    private val _webServiceError = MutableLiveData<List<String>>()
    val webServiceError: LiveData<List<String>>
        get() = _webServiceError

    private val _userCreationError = MutableLiveData<String>()
    val userCreationError: LiveData<String>
        get() = _userCreationError

    private val _userCreatedSuccessfully = MutableLiveData<UserEntity>()
    val userEntityCreatedSuccessfully: LiveData<UserEntity>
        get() = _userCreatedSuccessfully

    private val _userCreatedLocallySuccessfully = MutableLiveData<Boolean>()
    val userCreatedLocallySuccessfully: LiveData<Boolean>
        get() = _userCreatedLocallySuccessfully

    private val _openHomeFragment = MutableLiveData<Boolean>()
    val openHomeFragment: LiveData<Boolean>
        get() = _openHomeFragment

    init {
        accessPin.value = ""
        confirmAccessPin.value = ""
        _webServiceError.value = ArrayList()
        _userCreatedSuccessfully.value = null
        _userCreatedLocallySuccessfully.value = null
        _openHomeFragment.value = null
    }

    fun openHomeFragment() {
        _openHomeFragment.value = true
    }

    fun onOpenedHomeFragment() {
        _openHomeFragment.value = null
    }

    fun getFirebaseId(): String {
        return repository.getFirebaseId()
    }

    fun getLanguage(): String {
        return repository.getLanguage()
    }

    fun createdUserPref() {
        repository.createdUserPref()
    }

    fun setFreeTrialPref(subscription: Boolean) {
        viewModelScope.launch {
            repository.setFreeTrialPref(subscription)
        }
    }

    //region Implementation IContractAccessPin.ViewModel
    fun createAccount(createAccountReqDTO: CreateAccountReqDTO) {
        viewModelScope.launch {
            try {
                val response = repository.createAccount(createAccountReqDTO)

                if (response.isSuccessful) {
                    _userCreatedSuccessfully.value =
                        CreateAccountResDTO.toUserModel(
                            response.body()!!,
                            createAccountReqDTO.firebaseId,
                            createAccountReqDTO.accessPin,
                            createAccountReqDTO.status
                        )
                    repository.saveSecretKey(response.body()!!.secretKey)

                } else {
                    when (response.code()) {
                        Constants.CodeHttp.UNPROCESSABLE_ENTITY.code -> _webServiceError.value =
                            repository.getUnprocessableEntityError(response)
                        else -> _webServiceError.value = repository.getError(response)
                    }
                }
            } catch (ex: Exception) {
                Timber.d(ex)
                val error = context.getString(R.string.text_fail)
                _webServiceError.value = arrayListOf(error)
            }
        }
    }

    fun createUser(userEntity: UserEntity) {
        viewModelScope.launch {
            try {
                repository.createUser(userEntity)
                _userCreatedLocallySuccessfully.value = true
            } catch (ex: Exception) {
                Timber.d(ex)
                val error = context.getString(R.string.text_fail)
                _userCreationError.value = error
            }
        }
    }

    fun updateAccessPin(newAccessPin: String, firebaseId: String) {
        viewModelScope.launch {
            try {
                repository.updateAccessPin(newAccessPin, firebaseId)
                _userCreatedLocallySuccessfully.value = true
            } catch (ex: Exception) {
                Timber.d(ex)
                val error = context.getString(R.string.text_fail)
                _userCreationError.value = error
            }
        }
    }

    //endregion
}
