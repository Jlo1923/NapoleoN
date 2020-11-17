package com.naposystems.napoleonchat.ui.recoveryOlderAccountQuestions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.dto.recoveryOlderAccountQuestions.RecoveryOlderAccountDTO
import com.naposystems.napoleonchat.dto.recoveryOlderAccountQuestions.getQuestions.RecoveryOlderAccountQuestionsResDTO
import com.naposystems.napoleonchat.model.recoveryOlderAccount.RecoveryOlderAccountQuestions
import com.naposystems.napoleonchat.repository.recoveryOlderAccountQuestionsRepository.RecoveryOlderAccountQuestionsRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class RecoveryOlderAccountQuestionsViewModel @Inject constructor(
    private val repository: RecoveryOlderAccountQuestionsRepository
) : ViewModel(), IContractRecoveryOlderAccountQuestions.ViewModel {

    private val _recoveryOlderAccountQuestions = MutableLiveData<RecoveryOlderAccountQuestions>()
    val recoveryOlderAccountQuestions: LiveData<RecoveryOlderAccountQuestions>
        get() = _recoveryOlderAccountQuestions

    private var _accountCreatedSuccess = MutableLiveData<RecoveryOlderAccountDTO>()
    val accountCreatedSuccess: LiveData<RecoveryOlderAccountDTO>
        get() = _accountCreatedSuccess

    private val _recoveryOlderQuestionsCreatingError = MutableLiveData<List<String>>()
    val recoveryOlderQuestionsCreatingError: LiveData<List<String>>
        get() = _recoveryOlderQuestionsCreatingError

    private val _recoveryOlderAnswersCreatingError = MutableLiveData<List<String>>()
    val recoveryOlderAnswersCreatingError: LiveData<List<String>>
        get() = _recoveryOlderAnswersCreatingError

    private val _recoveryAnswersCreatingErrors = MutableLiveData<List<String>>()
    val recoveryAnswersCreatingErrors: LiveData<List<String>>
        get() = _recoveryAnswersCreatingErrors

    override fun getOlderQuestions(nickname: String) {
        viewModelScope.launch {
            try {
                val response = repository.getOlderQuestions(nickname)

                if (response.isSuccessful) {
                    _recoveryOlderAccountQuestions.value =
                        RecoveryOlderAccountQuestionsResDTO.model(response.body()!!)

                } else {
                    when (response.code()) {
                        422 -> {
                            response.errorBody()?.let {responseBody ->
                                _recoveryOlderQuestionsCreatingError.value =
                                    repository.get422Error(responseBody)
                            }
                        }
                        else -> {
                            _recoveryOlderQuestionsCreatingError.value =
                                repository.getDefaultQuestionsError(response)
                        }
                    }
                }

            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    override fun sendAnswers(nickname: String, answerOne: String, answerTwo: String) {
        viewModelScope.launch {
            try {
                val response = repository.sendAnswers(nickname, answerOne, answerTwo)

                if (response.isSuccessful){
                    response.body()?.let { body ->
                        repository.insertUser(body.user)
                        repository.setRecoveredAccountPref()
                        repository.saveSecretKey(body.user.secretKey)
                        _accountCreatedSuccess.value = body.user
                    }
                } else {
                    when (response.code()) {
                        422 -> {
                            _recoveryAnswersCreatingErrors.value =
                                repository.get422Error(response.errorBody()!!)
                        }
                        else -> {
                            _recoveryAnswersCreatingErrors.value =
                                repository.getDefaultAnswersError(response)
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

    override fun resetRecoveryQuestions() {
        _recoveryOlderAccountQuestions.value = null
    }
}
