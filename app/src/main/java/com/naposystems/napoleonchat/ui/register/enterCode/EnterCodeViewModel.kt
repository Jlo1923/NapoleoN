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

class EnterCodeViewModel @Inject constructor(private val repository: EnterCodeRepository) :
    ViewModel(), IContractEnterCode.ViewModel {

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
    override fun sendCode(code: String) {
        viewModelScope.launch {
            try {
                val response = repository.sendCodeToWs(code)

                if (response.isSuccessful) {
                    _isValidCode.value = true
                    repository.saveAccountStatus(Constants.AccountStatus.CODE_VALIDATED.id)
                } else {
                    when (response.code()) {
                        400 -> {
                            _attemptsEnterCode.value = repository.getAttemptsForRetryCode()
                            _attemptsEnterCode.value = _attemptsEnterCode.value!!.inc()
                            repository.setAttemptsForRetryCode(_attemptsEnterCode.value!!)
                            _invalidCode.value = true
                        }
                        422 -> {
                            _isValidCode.value = false
                            _responseErrors.value = repository.get422Error(response)
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

    override fun codeForwarding() {
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
                        422 -> {
                            _responseErrors.value = repository.get422ErrorSendCode(response)
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

    override fun getAttemptsForRetryCode() {
        _attemptsEnterCode.value = repository.getAttemptsForRetryCode()
    }

    override fun getAttemptsForNewCode() {
        _attemptsForNewCode.value = repository.getAttemptsForNewCode()
    }

    override fun getNumAttemptsForNewCode(): Int {
        return repository.getAttemptsForNewCode()
    }

    override fun getTimeForNewCode(): Long {
        return repository.getTimeForNewCode()
    }

    override fun setTimeForRetryCode(timeWait: Int): Long {
        val time = System.currentTimeMillis() + timeWait
        repository.setTimeForRetryCode(time)
        return time
    }

    override fun setTimeForNewCode(timeWait: Int): Long {
        val time = System.currentTimeMillis() + timeWait
        repository.setTimeForNewCode(time)
        return time
    }

    override fun resetAttemptsEnterCode() {
        repository.resetAttemptsEnterCode()
    }

    override fun resetAttemptsNewCode() {
        repository.resetAttemptsNewCode()
    }

    //endregion
}
