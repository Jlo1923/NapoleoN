package com.naposystems.pepito.di.modules

import com.naposystems.pepito.repository.enterCode.EnterCodeRepository
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.webService.NapoleonApi
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