package com.naposystems.napoleonchat.repository.recoveryAccount

import com.naposystems.napoleonchat.service.socketClient.SocketClient
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.recoveryAccount.RecoveryAccountErrorDTO
import com.naposystems.napoleonchat.source.remote.dto.recoveryAccount.RecoveryAccountUserTypeResDTO
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.squareup.moshi.Moshi
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class RecoveryAccountRepositoryImp
@Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val socketClient: SocketClient
) : RecoveryAccountRepository {

    override suspend fun getUserType(nickname: String): Response<RecoveryAccountUserTypeResDTO> {
        return napoleonApi.getRecoveryQuestions(nickname)
    }

    override suspend fun setFirebaseId(newToken: String) {
        sharedPreferencesManager.putString(Constants.SharedPreferences.PREF_FIREBASE_ID, newToken)
        //update new token in pusher
        socketClient.setNewPusher(newToken)
    }

    override fun getError(response: ResponseBody): ArrayList<String> {
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(RecoveryAccountErrorDTO::class.java)
        val errorJson = adapter.fromJson(response.string())
        val errorList = ArrayList<String>()

        errorList.add(errorJson!!.error)
        return errorList
    }
}