package com.naposystems.napoleonchat.ui.registerRecoveryAccountQuestion

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.model.Questions
import com.naposystems.napoleonchat.model.RecoveryAnswer
import com.naposystems.napoleonchat.repository.registerRecoveryAccountQuestion.RegisterRecoveryAccountQuestionRepository
import com.naposystems.napoleonchat.source.remote.dto.registerRecoveryAccountQuestion.getQuestions.RegisterRecoveryAccountQuestionResDTO
import com.naposystems.napoleonchat.source.remote.dto.registerRecoveryAccountQuestion.sendAnswers.RegisterRecoveryAccountReqDTO
import com.naposystems.napoleonchat.utility.Constants
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class RegisterRecoveryAccountQuestionViewModel @Inject constructor(
    private val repository: RegisterRecoveryAccountQuestionRepository
) : ViewModel() {

    private val _questions = MutableLiveData<List<Questions>>()
    val questions: LiveData<List<Questions>>
        get() = _questions

    private val _webServiceError = MutableLiveData<List<String>>()
    val webServiceError: LiveData<List<String>>
        get() = _webServiceError

    private val _recoveryAnswerCreatingErrors = MutableLiveData<List<String>>()
    val recoveryAnswerCreatingErrors: LiveData<List<String>>
        get() = _recoveryAnswerCreatingErrors

    private val _recoveryAnswers = MutableLiveData<List<RecoveryAnswer>>()
    val recoveryAnswers: LiveData<List<RecoveryAnswer>>
        get() = _recoveryAnswers

    private val _countAnswers = MutableLiveData<Int>()
    val countAnswers: LiveData<Int>
        get() = _countAnswers

    private val _recoveryQuestionsSavedSuccessfully = MutableLiveData<Boolean>()
    val recoveryQuestionsSavedSuccessfully: LiveData<Boolean>
        get() = _recoveryQuestionsSavedSuccessfully

    init {
        _questions.value = ArrayList()
        _webServiceError.value = ArrayList()
        _recoveryAnswers.value = ArrayList()
        _countAnswers.value = 1
    }

    //region Implementation IContractRegisterRecoveryAccountQuestion.ViewModel

    fun addRecoveryAnswer(answer: RecoveryAnswer, isFinal: Int) {
        val mutableAnswers: MutableList<RecoveryAnswer> = ArrayList()

        mutableAnswers.addAll(_recoveryAnswers.value!!)

        mutableAnswers.add(answer)

        val filterQuestion = _questions.value!!.filter { it.id == answer.questionId }

        val mutableQuestions: MutableList<Questions> = ArrayList()
        mutableQuestions.addAll(_questions.value!!)

        mutableQuestions.remove(filterQuestion.first())

        _questions.value = mutableQuestions
        _recoveryAnswers.value = mutableAnswers
        if (isFinal != 0) {
            _countAnswers.value = _countAnswers.value!! + 1
        }
    }

    fun getQuestions() {
        viewModelScope.launch {
            try {
                val response = repository.getQuestions()

                if (response.isSuccessful) {
                    val mutableList: MutableList<Questions> = ArrayList()

                    mutableList.addAll(
                        RegisterRecoveryAccountQuestionResDTO
                            .toListQuestionEntity(response.body()!!)
                    )
                    _questions.value = mutableList

                } else {
                    _webServiceError.value = repository.getError(response.errorBody()!!)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun sendRecoveryAnswers() {
        viewModelScope.launch {
            try {
                val recoveryAnswers = RegisterRecoveryAccountReqDTO(
                    RecoveryAnswer.toListRegisterRecoveryAccountAnswerReqDTO(
                        _recoveryAnswers.value!!
                    )
                )
                val response = repository.sendRecoveryAnswers(recoveryAnswers)

                if (response.isSuccessful) {
                    _recoveryQuestionsSavedSuccessfully.value = true
                    repository.registeredQuestionsPref()
                } else {
                    when (response.code()) {
                        Constants.CodeHttp.UNPROCESSABLE_ENTITY.code -> {
                            _recoveryQuestionsSavedSuccessfully.value = false
                            _recoveryAnswerCreatingErrors.value =
                                repository.getUnprocessableEntityError(response.errorBody()!!)
                        }
                        else -> {
                            _recoveryQuestionsSavedSuccessfully.value = false
                            _recoveryAnswerCreatingErrors.value =
                                repository.getError(response.errorBody()!!)
                        }
                    }
                }

            } catch (e: Exception) {
                _recoveryQuestionsSavedSuccessfully.value = false
                Timber.e(e)
            }
        }
    }

    //endregion
}
