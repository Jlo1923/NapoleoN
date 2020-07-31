package com.naposystems.pepito.utility

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.naposystems.pepito.BuildConfig


class SharedPreferencesManager(private val context: Context) {

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
        editor.apply()
    }

}