package com.naposystems.pepito.ui.recoveryAccount

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.dto.recoveryAccount.RecoveryAccountResDTO
import com.naposystems.pepito.model.recoveryAccount.RecoveryQuestions
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class RecoveryAccountViewModel @Inject constructor(
    private val repository: IContractRecoveryAccount.Repository
) : ViewModel(), IContractRecoveryAccount.ViewModel {

    private val _recoveryQuestions = MutableLiveData<List<RecoveryQuestions>>()
    val recoveryQuestions: LiveData<List<RecoveryQuestions>>
        get() = _recoveryQuestions

    private val _recoveryQuestionsCreatingError = MutableLiveData<List<String>>()
    val recoveryQuestionsCreatingError: LiveData<List<String>>
        get() = _recoveryQuestionsCreatingError

    init {
        _recoveryQuestions.value = emptyList()
    }

    override fun sendNickname(nickname: String) {
        viewModelScope.launch {
            try {
                val response = repository.getRecoveryQuestions(nickname)

                if (response.isSuccessful) {
                    _recoveryQuestions.value =
                        RecoveryAccountResDTO.toListRecoveryQuestions(response.body()!!)
                } else {
                    when (response.code()) {
                        422 -> {}
                        else -> _recoveryQuestionsCreatingError.value =
                            repository.getError(response.errorBody()!!)

                    }
                }

            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    override fun resetRecoveryQuestions() {
        _recoveryQuestions.value = emptyList()
    }
}
