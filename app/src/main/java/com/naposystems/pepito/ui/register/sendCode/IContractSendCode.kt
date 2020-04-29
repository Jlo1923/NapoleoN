package com.naposystems.pepito.ui.register.sendCode

import com.naposystems.pepito.dto.sendCode.SendCodeReqDTO
import com.naposystems.pepito.dto.sendCode.SendCodeResDTO
import kotlinx.coroutines.Deferred
import retrofit2.Response

interface IContractSendCode {

    interface ViewModel {
        fun requestCode()
        fun resetCode()
        fun getTimeForNewCode()
        fun getTimeForEnterCode()
        fun getAttemptsNewCode(): Int
        fun getAttemptsEnterCode(): Int
        fun resetAttemptsEnterCode()
        fun resetAttemptsNewCode()
        fun setFirebaseId(newToken : String)
    }

    interface Repository {
        suspend fun requestCode(): Response<SendCodeResDTO>
        suspend fun setFirebaseId(newToken : String)
        fun getTimeForNewCode(): Long
        fun getTimeForEnterCode(): Long
        fun setAttemptNewCode()
        fun getAttemptsNewCode(): Int
        fun getAttemptsEnterCode(): Int
        fun resetAttemptsEnterCode()
        fun resetAttemptsNewCode()
        fun get422Error(response: Response<SendCodeResDTO>): ArrayList<String>
        fun getDefaultError(response: Response<SendCodeResDTO>): ArrayList<String>
    }
}