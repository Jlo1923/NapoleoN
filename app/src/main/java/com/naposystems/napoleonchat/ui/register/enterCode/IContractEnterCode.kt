package com.naposystems.napoleonchat.ui.register.enterCode

import com.naposystems.napoleonchat.dto.enterCode.EnterCodeResDTO
import com.naposystems.napoleonchat.dto.sendCode.SendCodeResDTO
import retrofit2.Response

interface IContractEnterCode {

    interface ViewModel {
        fun sendCode(code: String)
        fun setTimeForRetryCode(timeWait: Int): Long
        fun getAttemptsForRetryCode()
        fun getAttemptsForNewCode()
        fun getNumAttemptsForNewCode() : Int
        fun getTimeForNewCode(): Long
        fun setTimeForNewCode(timeWait: Int): Long
        fun codeForwarding()
        fun resetAttemptsEnterCode()
        fun resetAttemptsNewCode()
    }

    interface Repository {
        suspend fun sendCodeToWs(code: String): Response<EnterCodeResDTO>
        suspend fun codeForwarding(): Response<SendCodeResDTO>
        fun getAttemptsForNewCode(): Int
        fun getAttemptsForRetryCode(): Int
        fun getTimeForNewCode(): Long
        fun setAttemptsForNewCode(attempts: Int)
        fun setTimeForNewCode(time: Long)
        fun setAttemptsForRetryCode(attempts: Int)
        fun setTimeForRetryCode(time: Long)
        fun resetAttemptsEnterCode()
        fun resetAttemptsNewCode()
        fun get422Error(response: Response<EnterCodeResDTO>): ArrayList<String>
        fun getDefaultError(response: Response<EnterCodeResDTO>): ArrayList<String>
        fun get422ErrorSendCode(response: Response<SendCodeResDTO>): ArrayList<String>
        fun getDefaultErrorSendCode(response: Response<SendCodeResDTO>): ArrayList<String>
        fun saveAccountStatus(id: Int)
    }
}