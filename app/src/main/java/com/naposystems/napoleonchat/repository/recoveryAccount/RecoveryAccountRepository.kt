package com.naposystems.napoleonchat.repository.recoveryAccount

import com.naposystems.napoleonchat.source.remote.dto.recoveryAccount.RecoveryAccountUserTypeResDTO
import okhttp3.ResponseBody
import retrofit2.Response

interface RecoveryAccountRepository {
    suspend fun getUserType(nickname: String): Response<RecoveryAccountUserTypeResDTO>
    fun getError(response: ResponseBody): ArrayList<String>
    suspend fun setFirebaseId(newToken: String)
}