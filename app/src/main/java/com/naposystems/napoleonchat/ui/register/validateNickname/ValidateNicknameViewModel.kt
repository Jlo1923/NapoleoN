package com.naposystems.napoleonchat.ui.register.validateNickname

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.source.remote.dto.sendCode.SendCodeErrorDTO
import com.naposystems.napoleonchat.source.remote.dto.validateNickname.ValidateNicknameReqDTO
import com.naposystems.napoleonchat.repository.validateNickname.ValidateNicknameRepository
import com.squareup.moshi.Moshi
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ValidateNicknameViewModel
@Inject constructor(
    private val repository: ValidateNicknameRepository
) : ViewModel(), IContractValidateNickname.ViewModel {

    val nickName = MutableLiveData<String>()
    val displayName = MutableLiveData<String>()

    private val _itsNicknameValid = MutableLiveData<Boolean>()
    val itsNicknameValid: LiveData<Boolean>
        get() = _itsNicknameValid

    private val _webServiceError = MutableLiveData<String>()
    val webServiceError: LiveData<String>
        get() = _webServiceError

    private val _openTermsAndConditions = MutableLiveData<Boolean>()
    val openTermsAndConditions: LiveData<Boolean>
        get() = _openTermsAndConditions

    init {
        nickName.value = ""
        displayName.value = ""
        _openTermsAndConditions.value = false
        _itsNicknameValid.value = null
        _webServiceError.value = ""
    }

    fun onTermsAndConditionsPressed() {
        _openTermsAndConditions.value = true
    }

    fun onTermsAndConditionsLaunched() {
        _openTermsAndConditions.value = null
    }

    //region Implementation IContractCreateAccount.ViewModel
    override fun validateNickname(validateNicknameReqDTO: ValidateNicknameReqDTO) {
        viewModelScope.launch {
            try {
                val response = repository.validateNickname(validateNicknameReqDTO)

                if (response.isSuccessful) {
                    response.body()?.let {
                        _itsNicknameValid.value = it.nicknameExist
                    }
                } else {
                    when (response.code()) {
                        422 -> {

                            val moshi = Moshi.Builder().build()

                            val adapter = moshi.adapter(SendCodeErrorDTO::class.java)

                            val sendCodeErrorDTO = adapter.fromJson(response.errorBody()!!.string())

                            _webServiceError.value = sendCodeErrorDTO!!.error
                        }
                        else -> {
                            //TODO:change text
                            _webServiceError.value = "Unexpected error"
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
                //TODO:change text
                _webServiceError.value = "Unexpected error"
            }
        }
    }

    override fun setNoValidNickname() {
        _itsNicknameValid.value = true
    }

    //endregion
}
