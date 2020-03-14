package com.naposystems.pepito.ui.registerRecoveryAccount

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.naposystems.pepito.repository.registerRecoveryAccount.RegisterRecoveryAccountRepository
import javax.inject.Inject

class RegisterRecoveryAccountViewModel @Inject constructor(
    private val repository: RegisterRecoveryAccountRepository
) : ViewModel(), IContractRegisterRecoveryAccount.ViewModel {

    private val _recoveryQuestionsPref = MutableLiveData<Int>()
    val recoveryQuestionsPref: LiveData<Int>
        get() = _recoveryQuestionsPref

    init {
        _recoveryQuestionsPref.value = null
    }

    override fun getRecoveryQuestionsPref() {
        _recoveryQuestionsPref.value = repository.getRecoveryQuestionsPref()
    }
}
