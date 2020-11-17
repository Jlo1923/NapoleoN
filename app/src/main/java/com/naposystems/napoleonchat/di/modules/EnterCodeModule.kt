package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.repository.enterCode.EnterCodeRepository
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.webService.NapoleonApi
import dagger.Module
import dagger.Provides

@Module
class EnterCodeModule {

    @Provides
    fun provideRepository(
        napoleonApi: NapoleonApi,
        sharedPreferencesManager: SharedPreferencesManager
    ): EnterCodeRepository {
        return EnterCodeRepository(napoleonApi, sharedPreferencesManager)
    }
}