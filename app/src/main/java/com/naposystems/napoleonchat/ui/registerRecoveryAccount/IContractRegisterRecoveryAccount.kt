package com.naposystems.napoleonchat.ui.registerRecoveryAccount

interface IContractRegisterRecoveryAccount {
    interface ViewModel {
        fun getRecoveryQuestionsPref()
    }

    interface Repository {
        fun getRecoveryQuestionsPref(): Int
    }
}