package com.naposystems.pepito.di.modules

import com.naposystems.pepito.repository.sendCode.SendCodeRepository
import com.naposystems.pepito.webService.NapoleonApi
import dagger.Module
import dagger.Provides

@Module
class SendCodeModule {

    @Provides
    fun provideRepository(
        napoleonApi: NapoleonApi
    ): SendCodeRepository {
        return SendCodeRepository(napoleonApi)
    }
}