package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.repository.accessPin.CreateAccountRepository
import com.naposystems.pepito.repository.validateNickname.ValidateNicknameRepository
import com.naposystems.pepito.webService.NapoleonApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class CreateAccountModule {

    @Provides
    @Singleton
    fun provideRepository(userLocalDataSource: UserLocalDataSource, napoleonApi: NapoleonApi): CreateAccountRepository {
        return CreateAccountRepository(userLocalDataSource, napoleonApi)
    }
}