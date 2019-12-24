package com.naposystems.pepito.ui.registerRecoveryAccount

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.naposystems.pepito.repository.registerRecoveryAccount.RegisterRecoveryAccountRepository
import javax.inject.Inject

class RegisterRecoveryAccountViewModel @Inject constructor(
    private val repository: RegisterRecoveryAccountRepository
) : ViewModel(), IContractRegisterRecoveryAccount.ViewModel {

    private val _flagRecoveryQuestions = MutableLiveData<Int>()
    val flagRecoveryQuestions: LiveData<Int>
        get() = _flagRecoveryQuestions

    init {
        _flagRecoveryQuestions.value = null
    }

    override fun getFlagRecoveryQuestions() {
        _flagRecoveryQuestions.value = repository.getFlagRecoveryQuestions()
    }



}
