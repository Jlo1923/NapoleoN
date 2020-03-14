package com.naposystems.pepito.ui.recoveryAccount

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.dto.recoveryAccount.RecoveryAccountResDTO
import com.naposystems.pepito.dto.recoveryAccount.RecoveryAccountUserTypeResDTO
import com.naposystems.pepito.model.recoveryAccount.RecoveryAccountUserType
import com.naposystems.pepito.model.recoveryAccount.RecoveryQuestions
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class RecoveryAccountViewModel @Inject constructor(
    private val repository: IContractRecoveryAccount.Repository
) : ViewModel(), IContractRecoveryAccount.ViewModel {

    /*private val _recoveryQuestions = MutableLiveData<List<RecoveryQuestions>>()
    val recoveryQuestions: LiveData<List<RecoveryQuestions>>
        get() = _recoveryQuestions*/

    private val _userType = MutableLiveData<RecoveryAccountUserType>()
    val userType: LiveData<RecoveryAccountUserType>
        get() = _userType

    private val _recoveryAttempts = MutableLiveData<Int>()
    val recoveryAttempts: LiveData<Int>
        get() = _recoveryAttempts

    private val _recoveryQuestionsCreatingError = MutableLiveData<List<String>>()
    val recoveryQuestionsCreatingError: LiveData<List<String>>
        get() = _recoveryQuestionsCreatingError

    override fun sendNickname(nickname: String) {
        viewModelScope.launch {
            try {
                val response = repository.getUserType(nickname)

                if (response.isSuccessful) {
                    _userType.value = RecoveryAccountUserTypeResDTO.toModel(response.body()!!)

                } else {
                    when (response.code()) {
                        422 -> {}
                        else -> {
                            _recoveryQuestionsCreatingError.value =
                                repository.getError(response.errorBody()!!)
                        }
                    }
                }

            } catch (e: Exception) {
                Timber.d(e)
            }
        }
    }

    override fun getRecoveryAttempts() {
        viewModelScope.launch {
            _recoveryAttempts.value = repository.getRecoveryAttempts()
        }
    }

    override fun resetRecoveryQuestions() {
        _userType.value = null
    }
}
