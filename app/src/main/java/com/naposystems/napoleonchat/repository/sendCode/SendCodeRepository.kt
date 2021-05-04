package com.naposystems.napoleonchat.repository.sendCode

import com.naposystems.napoleonchat.source.remote.dto.sendCode.SendCodeResDTO
import retrofit2.Response

interface SendCodeRepository {

    suspend fun requestCode(): Response<SendCodeResDTO>
    suspend fun setFirebaseId(newToken: String)
    fun getTimeForNewCode(): Long
    fun getTimeForEnterCode(): Long
    fun setAttemptNewCode()
    fun getAttemptsNewCode(): Int
    fun getAttemptsEnterCode(): Int
    fun resetAttemptsEnterCode()
    fun resetAttemptsNewCode()
    fun getUnprocessableEntityError(response: Response<SendCodeResDTO>): ArrayList<String>
    fun getDefaultError(response: Response<SendCodeResDTO>): ArrayList<String>

}