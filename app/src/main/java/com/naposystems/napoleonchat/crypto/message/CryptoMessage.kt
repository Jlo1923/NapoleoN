package com.naposystems.napoleonchat.crypto.message

import android.content.Context
import com.naposystems.napoleonchat.crypto.Crypto
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import timber.log.Timber

class CryptoMessage constructor(context: Context) : IContractCryptoMessage {

    val crypto = Crypto()
    val sharedPreferencesManager = SharedPreferencesManager(context)
    val secretKey: String by lazy {
        sharedPreferencesManager.getString(Constants.SharedPreferences.PREF_SECRET_KEY, "")
    }

    //region Implementation IContractCryptoMessage
    override fun decryptMessageBody(body: String): String {
        return try {
            if (body.isEmpty()) "" else crypto.decryptCipherTextWithRandomIV(body, secretKey)
        } catch (e: Exception) {
            Timber.e(e)
            body
        }
    }

    override fun encryptMessageBody(body: String): String {
        return crypto.encryptPlainTextWithRandomIV(body, secretKey)
    }
    //endregion
}