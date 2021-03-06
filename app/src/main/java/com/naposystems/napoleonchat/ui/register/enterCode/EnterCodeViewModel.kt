package com.naposystems.napoleonchat.ui.register.enterCode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.repository.enterCode.EnterCodeRepository
import com.naposystems.napoleonchat.utility.Constants
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class EnterCodeViewModel
@Inject constructor(
    private val repository: EnterCodeRepository
) : ViewModel() {

    private val _attemptsEnterCode = MutableLiveData<Int>()
    val attemptsEnterCode: LiveData<Int>
        get() = _attemptsEnterCode

    private val _attemptsForNewCode = MutableLiveData<Int>()
    val attemptsForNewCode: LiveData<Int>
        get() = _attemptsForNewCode

    private val _isValidCode = MutableLiveData<Boolean>()
    val isValidCode: LiveData<Boolean>
        get() = _isValidCode

    private val _forwardedCode = MutableLiveData<Boolean>()
    val forwardedCode: LiveData<Boolean>
        get() = _forwardedCode

    private val _invalidCode = MutableLiveData<Boolean>()
    val invalidCode: LiveData<Boolean>
        get() = _invalidCode

    private val _responseErrors = MutableLiveData<List<String>>()
    val responseErrors: LiveData<List<String>>
        get() = _responseErrors

    //region Implementation IContractEnterCode.ViewModel
    fun sendCode(code: String) {
        viewModelScope.launch {
            try {
                val response = repository.sendCodeToWs(code)

                if (response.isSuccessful) {
                    _isValidCode.value = true
                    repository.saveAccountStatus(Constants.AccountStatus.CODE_VALIDATED.id)
                } else {
                    when (response.code()) {
                        Constants.CodeHttp.BAD_REQUEST.code -> {
                            _attemptsEnterCode.value = repository.getAttemptsForRetryCode()
                            _attemptsEnterCode.value = _attemptsEnterCode.value!!.inc()
                            repository.setAttemptsForRetryCode(_attemptsEnterCode.value!!)
                            _invalidCode.value = true
                        }
                        Constants.CodeHttp.UNPROCESSABLE_ENTITY.code -> {
                            _isValidCode.value = false
                            _responseErrors.value = repository.getUnprocessableEntityError(response)
                        }
                        else -> {
                            _isValidCode.value = false
                            _responseErrors.value = repository.getDefaultError(response)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
                //TODO:change text
                _responseErrors.value = arrayListOf("Unexpected error")
            }
        }
    }

    fun codeForwarding() {
        viewModelScope.launch {
            try {
                val response = repository.codeForwarding()

                if (response.isSuccessful) {
                    _attemptsForNewCode.value = repository.getAttemptsForNewCode()
                    _attemptsForNewCode.value = _attemptsForNewCode.value!!.inc()
                    repository.setAttemptsForNewCode(_attemptsForNewCode.value!!)
                    _forwardedCode.value = true
                } else {
                    when (response.code()) {
                        Constants.CodeHttp.UNPROCESSABLE_ENTITY.code -> {
                            _responseErrors.value =
                                repository.getUnprocessableEntityErrorSendCode(response)
                            _forwardedCode.value = false
                        }
                        else -> {
                            _responseErrors.value = repository.getDefaultErrorSendCode(response)
                            _forwardedCode.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
                //TODO:change text
                _responseErrors.value = arrayListOf("Unexpected error")
            }
        }
    }

    fun getAttemptsForRetryCode() {
        _attemptsEnterCode.value = repository.getAttemptsForRetryCode()
    }

    fun getAttemptsForNewCode() {
        _attemptsForNewCode.value = repository.getAttemptsForNewCode()
    }

    fun getNumAttemptsForNewCode(): Int {
        return repository.getAttemptsForNewCode()
    }

    fun getTimeForNewCode(): Long {
        return repository.getTimeForNewCode()
    }

    fun setTimeForRetryCode(timeWait: Int): Long {
        val time = System.currentTimeMillis() + timeWait
        repository.setTimeForRetryCode(time)
        return time
    }

    fun setTimeForNewCode(timeWait: Int): Long {
        val time = System.currentTimeMillis() + timeWait
        repository.setTimeForNewCode(time)
        return time
    }

    fun resetAttemptsEnterCode() {
        repository.resetAttemptsEnterCode()
    }

    fun resetAttemptsNewCode() {
        repository.resetAttemptsNewCode()
    }

    //endregion
}
