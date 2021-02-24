package com.naposystems.napoleonchat.utility

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.utility.Constants.SharedPreferences
import javax.inject.Inject

class SharedPreferencesManager
@Inject constructor(
    private val context: Context
) {

    private val sharedPreferences by lazy {
        if (BuildConfig.ENCRYPT_SHARED_PREFERENCES) {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences
                .create(
                    Constants.SharedPreferences.PREF_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
        } else {
            context.getSharedPreferences(
                Constants.SharedPreferences.PREF_NAME,
                Context.MODE_PRIVATE
            )
        }
    }

    fun putString(preferenceName: String, data: String) {
        with(sharedPreferences.edit()) {
            putString(
                preferenceName,
                data
            )
            commit()
        }
    }

    fun getString(preferenceName: String, defaultValue: String): String {
        return sharedPreferences.getString(
            preferenceName,
            defaultValue
        )!!
    }

    fun putBoolean(preferenceName: String, data: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(
                preferenceName,
                data
            )
            commit()
        }
    }

    fun getBoolean(preferenceName: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(
            preferenceName,
            defaultValue
        )
    }

    fun putInt(preferenceName: String, data: Int) {
        with(sharedPreferences.edit()) {
            putInt(
                preferenceName,
                data
            )
            commit()
        }
    }

    fun getInt(preferenceName: String): Int {
        return sharedPreferences.getInt(
            preferenceName,
            0
        )
    }

    fun putLong(preferenceName: String, data: Long) {
        with(sharedPreferences.edit()) {
            putLong(
                preferenceName,
                data
            )
            commit()
        }
    }

    fun getLong(preferenceName: String): Long {
        return sharedPreferences.getLong(
            preferenceName,
            0L
        )
    }

    fun reset() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.remove(SharedPreferences.PREF_NAME)
        editor.remove(SharedPreferences.PREF_LANGUAGE_SELECTED)
        editor.remove(SharedPreferences.PREF_ACCOUNT_STATUS)
        editor.remove(SharedPreferences.PREF_FIREBASE_ID)
//        editor.remove(SharedPreferences.PREF_USER_ID)
        editor.remove(SharedPreferences.PREF_COLOR_SCHEME)
        editor.remove(SharedPreferences.PREF_USER_DISPLAY_FORMAT)
        editor.remove(SharedPreferences.PREF_TIME_FORMAT)
        editor.remove(SharedPreferences.PREF_SELF_DESTRUCT_TIME)
        editor.remove(SharedPreferences.PREF_TIME_REQUEST_ACCESS_PIN)
        editor.remove(SharedPreferences.PREF_ALLOW_DOWNLOAD_ATTACHMENTS)
        editor.remove(SharedPreferences.PREF_SOCKET_ID)
        editor.remove(SharedPreferences.PREF_SECRET_KEY)
        editor.remove(SharedPreferences.PREF_OUTPUT_CONTROL)
        editor.remove(SharedPreferences.PREF_MESSAGE_SELF_DESTRUCT_TIME_NOT_SENT)
        editor.remove(SharedPreferences.PREF_JSON_NOTIFICATION)
        editor.remove(SharedPreferences.PREF_LAST_JSON_NOTIFICATION)
        editor.remove(SharedPreferences.PREF_ATTEMPTS_FOR_NEW_CODE)
        editor.remove(SharedPreferences.PREF_TIME_FOR_NEW_CODE)
        editor.remove(SharedPreferences.PREF_ATTEMPTS_FOR_RETRY_CODE)
        editor.remove(SharedPreferences.PREF_TIME_FOR_RETRY_CODE)
        editor.remove(SharedPreferences.PREF_ACCOUNT_RECOVERY_ATTEMPTS)
        editor.remove(SharedPreferences.PREF_RECOVERY_QUESTIONS_SAVED)
        editor.remove(SharedPreferences.PREF_EXISTING_ATTACK)
        editor.remove(SharedPreferences.PREF_ATTACKER_ID)
        editor.remove(SharedPreferences.PREF_FREE_TRIAL)
        editor.remove(SharedPreferences.PREF_TYPE_SUBSCRIPTION)
        editor.remove(SharedPreferences.PREF_SUBSCRIPTION_TIME)
        editor.remove(SharedPreferences.PREF_LOCK_STATUS)
        editor.remove(SharedPreferences.PREF_TYPE_LOCK_APP)
        editor.remove(SharedPreferences.PREF_BIOMETRICS_OPTION)
        editor.remove(SharedPreferences.PREF_LOCK_TIME_APP)
        editor.remove(SharedPreferences.PREF_UNLOCK_TIME_APP)
        editor.remove(SharedPreferences.PREF_UNLOCK_ATTEMPTS)
        editor.remove(SharedPreferences.PREF_UNLOCK_TOTAL_ATTEMPTS)
        editor.remove(SharedPreferences.PREF_SHOW_CASE_FIRST_STEP_HAS_BEEN_SHOW)
        editor.remove(SharedPreferences.PREF_SHOW_CASE_SECOND_STEP_HAS_BEEN_SHOW)
        editor.remove(SharedPreferences.PREF_SHOW_CASE_THIRD_STEP_HAS_BEEN_SHOW)
        editor.remove(SharedPreferences.PREF_SHOW_CASE_FOURTH_STEP_HAS_BEEN_SHOW)
        editor.remove(SharedPreferences.PREF_SHOW_CASE_FIFTH_STEP_HAS_BEEN_SHOW)
        editor.remove(SharedPreferences.PREF_SHOW_CASE_SIXTH_STEP_HAS_BEEN_SHOW)
        editor.remove(SharedPreferences.PREF_SHOW_CASE_SEVENTH_STEP_HAS_BEEN_SHOW)
        editor.apply()
    }

}