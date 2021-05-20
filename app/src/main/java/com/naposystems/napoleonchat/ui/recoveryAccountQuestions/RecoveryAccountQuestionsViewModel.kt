package com.naposystems.napoleonchat.ui.recoveryAccountQuestions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.model.recoveryAccountQuestions.RecoveryAccountAnswers
import com.naposystems.napoleonchat.repository.recoveryAccountQuestions.RecoveryAccountQuestionsRepository
import com.naposystems.napoleonchat.utility.Constants
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class RecoveryAccountQuestionsViewModel @Inject constructor(
    private val repository: RecoveryAccountQuestionsRepository.Repository
) : ViewModel(), RecoveryAccountQuestionsRepository.ViewModel {

    private val _sendAnswersSuccessfully = MutableLiveData<Boolean>()
    val sendAnswersSuccessfully: LiveData<Boolean>
        get() = _sendAnswersSuccessfully

    private val _recoveryAnswers = MutableLiveData<List<RecoveryAccountAnswers>>()
    val recoveryAnswers: LiveData<List<RecoveryAccountAnswers>>
        get() = _recoveryAnswers

    private var _userAccountDisplayName = MutableLiveData<String>()
    val userAccountDisplayName: LiveData<String>
        get() = _userAccountDisplayName

    private val _recoveryAnswerCreatingErrors = MutableLiveData<List<String>>()
    val recoveryAnswerCreatingErrors: LiveData<List<String>>
        get() = _recoveryAnswerCreatingErrors

    init {
        _recoveryAnswers.value = ArrayList()
    }

    override fun addRecoveryAnswer(answer: RecoveryAccountAnswers) {
        val mutableAnswers: MutableList<RecoveryAccountAnswers> = ArrayList()

        mutableAnswers.addAll(_recoveryAnswers.value!!)
        mutableAnswers.add(answer)

        _recoveryAnswers.value = mutableAnswers
    }

    override fun sendRecoveryAnswers(nickname: String) {
        viewModelScope.launch {
            try {
                val recoveryAnswers =
                    RecoveryAccountAnswers.toListRecoveryAccountAnswersReqDTO(
                        _recoveryAnswers.value!!
                    )
                val response = repository.sendRecoveryAnswers(nickname, recoveryAnswers)

                if (response.isSuccessful) {
                    _sendAnswersSuccessfully.value = true

                    repository.saveSecretKey(response.body()!!.user.secretKey)

                    repository.insertUser(response.body()!!.user)

                    _userAccountDisplayName.value = response.body()?.user?.fullname ?: ""

                    repository.setRecoveredAccountPref()
                    repository.setRecoveredQuestionsPref()
//                    repository.setFreeTrialPref()
                } else {
                    when (response.code()) {
                        Constants.CodeHttp.UNPROCESSABLE_ENTITY.code -> {
                            _sendAnswersSuccessfully.value = false
                            _recoveryAnswerCreatingErrors.value =
                                repository.getUnprocessableEntityError(response.errorBody()!!)
                        }
                        else -> {
                            _sendAnswersSuccessfully.value = false
                            _recoveryAnswerCreatingErrors.value =
                                repository.getError(response.errorBody()!!)
                        }
                    }
                }
            } catch (e: Exception) {
                _sendAnswersSuccessfully.value = false
                Timber.e(e)
            }
        }
    }

    override fun setFreeTrialPref(subscription: Boolean) {
        viewModelScope.launch {
            repository.setFreeTrialPref(subscription)
        }
    }

    override fun setAttemptPref() {
        viewModelScope.launch {
            repository.setAttemptPref()
        }
    }
}
