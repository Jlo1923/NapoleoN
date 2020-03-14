package com.naposystems.pepito.ui.recoveryAccount

import com.naposystems.pepito.dto.recoveryAccount.RecoveryAccountResDTO
import com.naposystems.pepito.dto.recoveryAccount.RecoveryAccountUserTypeResDTO
import okhttp3.ResponseBody
import retrofit2.Response

interface IContractRecoveryAccount {
    interface ViewModel {
        fun sendNickname(nickname: String)
        fun resetRecoveryQuestions()
        fun getRecoveryAttempts()
    }

    interface Repository {
//        suspend fun getRecoveryQuestions(nickname: String): Response<List<RecoveryAccountResDTO>>
        suspend fun getUserType(nickname: String) : Response<RecoveryAccountUserTypeResDTO>
        suspend fun getRecoveryAttempts(): Int
        fun getError(response:ResponseBody): ArrayList<String>
    }
}