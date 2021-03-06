package com.naposystems.napoleonchat.ui.register.sendCode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.repository.sendCode.SendCodeRepository
import com.naposystems.napoleonchat.utility.Constants
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SendCodeViewModel
@Inject constructor(
    private val repository: SendCodeRepository
) : ViewModel() {

    private val _codeSuccess = MutableLiveData<Boolean>()
    val codeSuccess: LiveData<Boolean>
        get() = _codeSuccess

    private val _timeForNewCode = MutableLiveData<Long>()
    val timeForNewCode: LiveData<Long>
        get() = _timeForNewCode

    private val _timeForEnterCode = MutableLiveData<Long>()
    val timeForEnterCode: LiveData<Long>
        get() = _timeForEnterCode

    private val _webServiceError = MutableLiveData<List<String>>()
    val webServiceError: LiveData<List<String>>
        get() = _webServiceError

    private val _successToken = MutableLiveData<Boolean>()
    val successToken: LiveData<Boolean>
        get() = _successToken

    fun resetCode() {
        _successToken.value = null
        _codeSuccess.value = null
        _webServiceError.value = emptyList()
    }

    //region Implementation IContractSendCode.ViewModel
    fun requestCode() {
        viewModelScope.launch {
            try {
                val response = repository.requestCode()

                if (response.isSuccessful) {
                    repository.setAttemptNewCode()
                    _codeSuccess.value = true
                } else {
                    when (response.code()) {
                        Constants.CodeHttp.UNPROCESSABLE_ENTITY.code -> {
                            _webServiceError.value =
                                repository.getUnprocessableEntityError(response)
                            _codeSuccess.value = false
                        }
                        else -> {
                            _webServiceError.value = repository.getDefaultError(response)
                            _codeSuccess.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
                //TODO:change text
                _webServiceError.value = arrayListOf("Unexpected error")
            }
        }
    }

    fun getTimeForNewCode() {
        _timeForNewCode.value = repository.getTimeForNewCode()
    }

    fun getTimeForEnterCode() {
        _timeForEnterCode.value = repository.getTimeForEnterCode()
    }

    fun getAttemptsNewCode(): Int {
        return repository.getAttemptsNewCode()
    }

    fun getAttemptsEnterCode(): Int {
        return repository.getAttemptsEnterCode()
    }

    fun resetAttemptsEnterCode() {
        repository.resetAttemptsEnterCode()
    }

    fun resetAttemptsNewCode() {
        repository.resetAttemptsNewCode()
    }

    fun setFirebaseId(newToken: String) {
        viewModelScope.launch {
            repository.setFirebaseId(newToken)
            _successToken.value = true
        }
    }

    //endregion
}