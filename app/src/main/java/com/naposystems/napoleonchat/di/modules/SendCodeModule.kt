package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.repository.sendCode.SendCodeRepository
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.webService.NapoleonApi
import dagger.Module
import dagger.Provides

@Module
class SendCodeModule {

    @Provides
    fun provideRepository(
        napoleonApi: NapoleonApi,
        sharedPreferencesManager: SharedPreferencesManager
    ): SendCodeRepository {
        return SendCodeRepository(napoleonApi, sharedPreferencesManager)
    }
}