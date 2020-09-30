package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.repository.addContact.AddContactRepository
import com.naposystems.napoleonchat.ui.addContact.IContractAddContact
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.webService.NapoleonApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AddContactModule {

    @Provides
    @Singleton
    fun provideRepository(
        napoleonApi: NapoleonApi,
        userLocalDataSource: UserLocalDataSource,
        sharedPreferencesManager: SharedPreferencesManager
    ): IContractAddContact.Repository {
        return AddContactRepository(
            napoleonApi,
            userLocalDataSource,
            sharedPreferencesManager
        )
    }
}