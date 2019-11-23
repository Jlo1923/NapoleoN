package com.naposystems.pepito.ui.register.sendCode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.dto.sendCode.SendCodeErrorDTO
import com.naposystems.pepito.dto.sendCode.SendCodeReqDTO
import com.naposystems.pepito.repository.sendCode.SendCodeRepository
import com.squareup.moshi.Moshi
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SendCodeViewModel @Inject constructor(private val repository: SendCodeRepository) :
    ViewModel(), IContractSendCode.ViewModel {

    private val _openEnterCode = MutableLiveData<Boolean>()
    val openEnterCode: LiveData<Boolean>
        get() = _openEnterCode

    private val _webServiceError = MutableLiveData<String>()
    val webServiceError: LiveData<String>
        get() = _webServiceError

    init {
        _openEnterCode.value = null
        _webServiceError.value = ""
    }

    fun onSendCodePressed(sendCodeReqDTO: SendCodeReqDTO) {
        viewModelScope.launch {
            requestCode(sendCodeReqDTO)
        }
    }

    fun onEnterCodeOpened() {
        _openEnterCode.value = null
    }

    //region Implementation IContractSendCode.ViewModel
    override fun requestCode(sendCodeReqDTO: SendCodeReqDTO) {
        viewModelScope.launch {
            try {
                val response = repository.requestCode(sendCodeReqDTO)

                if (response.isSuccessful) {
                    _openEnterCode.value = true
                } else {
                    when (response.code()) {
                        422 -> {
                            val moshi = Moshi.Builder().build()

                            val adapter = moshi.adapter(SendCodeErrorDTO::class.java)

                            val sendCodeErrorDTO = adapter.fromJson(response.errorBody()!!.string())

                            _webServiceError.value = sendCodeErrorDTO!!.firebaseId[0]
                        }
                        else -> _webServiceError.value = "Error inesperado jajaja"
                    }
                }
            } catch (e: Exception) {
                Timber.d(e)
                _webServiceError.value = "Error inesperado jajaja"
            }
        }
    }
    //endregion
}