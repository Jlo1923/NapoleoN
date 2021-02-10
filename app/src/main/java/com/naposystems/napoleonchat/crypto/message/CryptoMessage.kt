package com.naposystems.napoleonchat.crypto.message

import android.content.Context
import com.naposystems.napoleonchat.crypto.Crypto
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import timber.log.Timber
import javax.inject.Inject

class CryptoMessage
@Inject constructor(
    private val crypto: Crypto,
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractCryptoMessage {

    //region Implementation IContractCryptoMessage
    override fun decryptMessageBody(body: String): String {
        return try {
            val secretKey: String =
                sharedPreferencesManager.getString(Constants.SharedPreferences.PREF_SECRET_KEY, "")
            if (body.isEmpty()) "" else crypto.decryptCipherTextWithRandomIV(body, secretKey)
        } catch (e: Exception) {
            Timber.e(e)
            body
        }
    }

    override fun encryptMessageBody(body: String): String {
        val secretKey: String =
            sharedPreferencesManager.getString(Constants.SharedPreferences.PREF_SECRET_KEY, "")
        return crypto.encryptPlainTextWithRandomIV(body, secretKey)
    }
    //endregion
}