package com.naposystems.pepito.ui.recoveryAccountQuestions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.model.recoveryAccountQuestions.RecoveryAccountAnswers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class RecoveryAccountQuestionsViewModel @Inject constructor(
    private val repository: IContractRecoveryAccountQuestions.Repository
) : ViewModel(), IContractRecoveryAccountQuestions.ViewModel {

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

                    repository.saveRecoveredAccountPref()
                    repository.insertUser(response.body()!!.user)

                    _userAccountDisplayName.value = response.body()!!.user.fullname

                } else {
                    when (response.code()) {
                        422 -> {
                            _sendAnswersSuccessfully.value = false
                            _recoveryAnswerCreatingErrors.value =
                                repository.get422Error(response.errorBody()!!)
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
}
