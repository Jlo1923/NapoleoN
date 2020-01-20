package com.naposystems.pepito.ui.registerRecoveryAccount

import retrofit2.Response

interface IContractRegisterRecoveryAccount {
    interface ViewModel {
        fun getRecoveryQuestionsPref()
    }

    interface Repository {
        fun getRecoveryQuestionsPref(): Int
    }
}