package com.naposystems.napoleonchat.ui.recoveryAccount

import com.naposystems.napoleonchat.dto.recoveryAccount.RecoveryAccountUserTypeResDTO
import okhttp3.ResponseBody
import retrofit2.Response

interface IContractRecoveryAccount {
    interface ViewModel {
        fun sendNickname(nickname: String)
        fun resetRecoveryQuestions()
        fun setFirebaseId(token: String)
    }

    interface Repository {
        suspend fun getUserType(nickname: String) : Response<RecoveryAccountUserTypeResDTO>
        fun getError(response:ResponseBody): ArrayList<String>
        suspend fun setFirebaseId(newToken : String)
    }
}