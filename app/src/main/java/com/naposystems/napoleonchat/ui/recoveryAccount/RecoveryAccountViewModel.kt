package com.naposystems.napoleonchat.ui.recoveryAccount

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.source.remote.dto.recoveryAccount.RecoveryAccountUserTypeResDTO
import com.naposystems.napoleonchat.model.recoveryAccount.RecoveryAccountUserType
import com.naposystems.napoleonchat.utility.Constants
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class RecoveryAccountViewModel @Inject constructor(
    private val repository: IContractRecoveryAccount.Repository
) : ViewModel(), IContractRecoveryAccount.ViewModel {

    private val _userType = MutableLiveData<RecoveryAccountUserType>()
    val userType: LiveData<RecoveryAccountUserType>
        get() = _userType

    private val _recoveryErrorForAttempts = MutableLiveData<List<String>>()
    val recoveryErrorForAttempts: LiveData<List<String>>
        get() = _recoveryErrorForAttempts

    private val _recoveryQuestionsCreatingError = MutableLiveData<List<String>>()
    val recoveryQuestionsCreatingError: LiveData<List<String>>
        get() = _recoveryQuestionsCreatingError

    private val _successToken = MutableLiveData<Boolean>()
    val successToken: LiveData<Boolean>
        get() = _successToken

    override fun sendNickname(nickname: String) {
        viewModelScope.launch {
            try {
                val response = repository.getUserType(nickname)

                if (response.isSuccessful) {
                    _userType.value = RecoveryAccountUserTypeResDTO.toModel(response.body()!!)

                } else {
                    when (response.code()) {
                        Constants.CodeHttp.FORBIDDEN.code -> {
                            _recoveryErrorForAttempts.value =
                                repository.getError(response.errorBody()!!)
                        }
                        else -> {
                            _recoveryQuestionsCreatingError.value = listOf("Error interno")
                        }
                    }
                }

            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    override fun resetRecoveryQuestions() {
        _userType.value = null
    }

    override fun setFirebaseId(token: String) {
        viewModelScope.launch {
            repository.setFirebaseId(token)
            _successToken.value = true
        }
    }
}
