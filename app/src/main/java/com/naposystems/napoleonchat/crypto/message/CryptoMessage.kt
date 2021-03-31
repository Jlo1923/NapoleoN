package com.naposystems.napoleonchat.crypto.message

import com.naposystems.napoleonchat.crypto.Crypto
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import timber.log.Timber
import javax.inject.Inject

class CryptoMessage
@Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractCryptoMessage {

    val crypto = Crypto()

    //region Implementation IContractCryptoMessage
    override fun decryptMessageBody(body: String): String {
        var info: String = body
        var decrypting = true

        var retries = 0

        while (decrypting || retries >= 10) {
            try {
                val secretKey: String =
                    sharedPreferencesManager.getString(
                        Constants.SharedPreferences.PREF_SECRET_KEY,
                        ""
                    )
                if (body.isEmpty())
                    info = ""
                else {
                    info = crypto.decryptCipherTextWithRandomIV(body, secretKey)
                    decrypting = false
                }

            } catch (e: Exception) {
                retries++
                Timber.e(e)
            }
        }

        return info

    }

    override fun encryptMessageBody(body: String): String {
        val secretKey: String =
            sharedPreferencesManager.getString(Constants.SharedPreferences.PREF_SECRET_KEY, "")
        return crypto.encryptPlainTextWithRandomIV(body, secretKey)
    }
    //endregion
}