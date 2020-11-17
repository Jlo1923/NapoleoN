package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.repository.accessPin.CreateAccountRepository
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.webService.NapoleonApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class CreateAccountModule {

    @Provides
    @Singleton
    fun provideRepository(

        userLocalDataSource: UserLocalDataSource,
        sharedPreferencesManager: SharedPreferencesManager,
        napoleonApi: NapoleonApi
    ): CreateAccountRepository {
        return CreateAccountRepository(userLocalDataSource, sharedPreferencesManager, napoleonApi)
    }
}