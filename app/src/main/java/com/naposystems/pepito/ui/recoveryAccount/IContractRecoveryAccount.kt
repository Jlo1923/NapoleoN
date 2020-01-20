package com.naposystems.pepito.ui.recoveryAccount

import com.naposystems.pepito.dto.recoveryAccount.RecoveryAccountResDTO
import okhttp3.ResponseBody
import retrofit2.Response

interface IContractRecoveryAccount {
    interface ViewModel {
        fun sendNickname(nickname: String)
        fun resetRecoveryQuestions()
    }

    interface Repository {
        suspend fun getRecoveryQuestions(nickname: String): Response<List<RecoveryAccountResDTO>>
        fun getError(response:ResponseBody): ArrayList<String>
    }
}