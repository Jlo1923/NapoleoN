package com.naposystems.napoleonchat.di.module.general

import android.content.Context
import com.naposystems.napoleonchat.crypto.Crypto
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class CryptoModule {

    @Provides
    @Singleton
    fun provideCryptoMessage(
        sharedPreferencesManager: SharedPreferencesManager
    ): CryptoMessage {
        return CryptoMessage( sharedPreferencesManager)
    }

}