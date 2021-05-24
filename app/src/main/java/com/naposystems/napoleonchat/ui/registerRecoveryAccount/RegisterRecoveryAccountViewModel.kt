package com.naposystems.napoleonchat.ui.registerRecoveryAccount

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.naposystems.napoleonchat.repository.registerRecoveryAccount.RegisterRecoveryAccountRepositoryImp
import javax.inject.Inject

class RegisterRecoveryAccountViewModel @Inject constructor(
    private val repository: RegisterRecoveryAccountRepositoryImp
) : ViewModel() {

    private val _recoveryQuestionsPref = MutableLiveData<Int>()
    val recoveryQuestionsPref: LiveData<Int>
        get() = _recoveryQuestionsPref

    init {
        _recoveryQuestionsPref.value = null
    }

    fun getRecoveryQuestionsPref() {
        _recoveryQuestionsPref.value = repository.getRecoveryQuestionsPref()
    }
}
