package com.naposystems.pepito.ui.registerRecoveryAccount

import retrofit2.Response

interface IContractRegisterRecoveryAccount {
    interface ViewModel {
        fun getFlagRecoveryQuestions()
    }

    interface Repository {
        fun getFlagRecoveryQuestions(): Int
    }
}