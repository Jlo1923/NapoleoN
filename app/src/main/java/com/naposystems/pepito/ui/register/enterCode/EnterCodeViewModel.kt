package com.naposystems.pepito.ui.register.enterCode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.dto.enterCode.EnterCodeReqDTO
import com.naposystems.pepito.repository.enterCode.EnterCodeRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class EnterCodeViewModel @Inject constructor(private val repository: EnterCodeRepository) :
    ViewModel(), IContractEnterCode.ViewModel {

    private val _attempts = MutableLiveData<Int>()
    val attempts: LiveData<Int>
        get() = _attempts

    private val _showInvalidCode = MutableLiveData<Boolean>()
    val showInvalidCode: LiveData<Boolean>
        get() = _showInvalidCode

    private val _itsCodeOk = MutableLiveData<Boolean>()
    val itsCodeOk: LiveData<Boolean>
        get() = _itsCodeOk

    private val _showErrors = MutableLiveData<List<String>>()
    val showErrors: LiveData<List<String>>
        get() = _showErrors

    init {
        _attempts.value = 0
        _showInvalidCode.value = false
        _itsCodeOk.value = false
        _showErrors.value = ArrayList()
    }

    fun increaseAttempts() {
        _attempts.value = _attempts.value!! + 1
    }

    fun resetShowInvalidCode() {
        _showInvalidCode.value = false
    }

    fun onContinueButtonPressed(enterCodeReqDTO: EnterCodeReqDTO) {
        viewModelScope.launch {
            sendCode(enterCodeReqDTO)
        }
    }

    //region Implementation IContractEnterCode.ViewModel
    override fun sendCode(enterCodeReqDTO: EnterCodeReqDTO) {
        viewModelScope.launch {
            try {
                val response = repository.sendCodeToWs(enterCodeReqDTO)

                if (response.isSuccessful) {
                    _itsCodeOk.value = true
                } else {
                    when (response.code()) {
                        400 -> _showInvalidCode.value = true
                        422 -> _showErrors.value = repository.get422Error(response)
                        else -> _showErrors.value = arrayListOf("Error inesperado")
                    }
                }
            } catch (e: Exception) {
                Timber.d(e)
                _showErrors.value = arrayListOf("Error inesperado")
            }
        }
    }
    //endregion
}
