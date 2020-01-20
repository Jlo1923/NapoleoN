package com.naposystems.pepito.di.modules

import com.naposystems.pepito.repository.validateNickname.ValidateNicknameRepository
import com.naposystems.pepito.webService.NapoleonApi
import dagger.Module
import dagger.Provides

@Module
class ValidateNicknameModule {

    @Provides
    fun provideRepository(napoleonApi: NapoleonApi): ValidateNicknameRepository {
        return ValidateNicknameRepository(napoleonApi)
    }
}