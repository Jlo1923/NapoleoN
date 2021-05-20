package com.naposystems.napoleonchat.repository.enterCode

import com.naposystems.napoleonchat.source.remote.dto.enterCode.EnterCodeResDTO
import com.naposystems.napoleonchat.source.remote.dto.sendCode.SendCodeResDTO
import retrofit2.Response

interface EnterCodeRepository {
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
    fun getUnprocessableEntityError(response: Response<EnterCodeResDTO>): ArrayList<String>
    fun getDefaultError(response: Response<EnterCodeResDTO>): ArrayList<String>
    fun getUnprocessableEntityErrorSendCode(response: Response<SendCodeResDTO>): ArrayList<String>
    fun getDefaultErrorSendCode(response: Response<SendCodeResDTO>): ArrayList<String>
    fun saveAccountStatus(id: Int)
}