package com.naposystems.pepito.ui.validatePasswordPreviousRecoveryAccount

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.repository.validatePasswordPreviousRecoveryAccount.ValidatePasswordPreviousRecoveryAccountRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ValidatePasswordPreviousRecoveryAccountViewModel @Inject constructor(
    private val repository: ValidatePasswordPreviousRecoveryAccountRepository
) : ViewModel(), IContractValidatePasswordPreviousRecoveryAccount.ViewModel {

    private val _passwordSuccess = MutableLiveData<Boolean>()
    val passwordSuccess: LiveData<Boolean>
        get() = _passwordSuccess

    private val _recoveryOlderPasswordCreatingError = MutableLiveData<List<String>>()
    val recoveryOlderPasswordCreatingError: LiveData<List<String>>
        get() = _recoveryOlderPasswordCreatingError

    override fun sendPassword(nickname: String, password: String) {
        viewModelScope.launch {
            try {
                val response = repository.sendPassword(nickname, password)

                if (response.isSuccessful) {
                    _passwordSuccess.value = response.body()!!.success
                } else {
                    when (response.code()) {
                        422 -> {
                            _passwordSuccess.value = false
                            _recoveryOlderPasswordCreatingError.value =
                                repository.get422Error(response)
                        }
                        else -> {
                            _passwordSuccess.value = false
                            _recoveryOlderPasswordCreatingError.value =
                                repository.getDefaultError(response)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    override fun setAttemptPref() {
        viewModelScope.launch {
            repository.setAttemptPref()
        }
    }
}
