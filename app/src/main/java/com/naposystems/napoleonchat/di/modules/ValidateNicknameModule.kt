package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.repository.validateNickname.ValidateNicknameRepository
import com.naposystems.napoleonchat.webService.NapoleonApi
import dagger.Module
import dagger.Provides

@Module
class ValidateNicknameModule {

    @Provides
    fun provideRepository(napoleonApi: NapoleonApi): ValidateNicknameRepository {
        return ValidateNicknameRepository(napoleonApi)
    }
}