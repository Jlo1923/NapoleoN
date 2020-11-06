package com.naposystems.napoleonchat.ui.register.sendCode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.repository.sendCode.SendCodeRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SendCodeViewModel @Inject constructor(private val repository: SendCodeRepository) :
    ViewModel(), IContractSendCode.ViewModel {

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

    override fun resetCode() {
        _successToken.value = null
        _codeSuccess.value = null
        _webServiceError.value = emptyList()
    }

    //region Implementation IContractSendCode.ViewModel
    override fun requestCode() {
        viewModelScope.launch {
            try {
                val response = repository.requestCode()

                if (response.isSuccessful) {
                    repository.setAttemptNewCode()
                    _codeSuccess.value = true
                } else {
                    when (response.code()) {
                        422 -> {
                            _webServiceError.value = repository.get422Error(response)
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

    override fun getTimeForNewCode() {
        _timeForNewCode.value = repository.getTimeForNewCode()
    }

    override fun getTimeForEnterCode() {
        _timeForEnterCode.value = repository.getTimeForEnterCode()
    }

    override fun getAttemptsNewCode(): Int {
        return repository.getAttemptsNewCode()
    }

    override fun getAttemptsEnterCode(): Int {
        return repository.getAttemptsEnterCode()
    }

    override fun resetAttemptsEnterCode() {
        repository.resetAttemptsEnterCode()
    }

    override fun resetAttemptsNewCode() {
        repository.resetAttemptsNewCode()
    }

    override fun setFirebaseId(newToken: String) {
        viewModelScope.launch {
            repository.setFirebaseId(newToken)
            _successToken.value = true
        }
    }

    //endregion
}