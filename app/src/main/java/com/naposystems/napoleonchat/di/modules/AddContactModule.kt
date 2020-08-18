package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.db.dao.contact.ContactDataSource
import com.naposystems.napoleonchat.db.dao.message.MessageDataSource
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
        contactLocalDataSource: ContactDataSource,
        messageLocalDataSource: MessageDataSource,
        userLocalDataSource: UserLocalDataSource,
        sharedPreferencesManager: SharedPreferencesManager
    ): IContractAddContact.Repository {
        return AddContactRepository(
            napoleonApi,
            contactLocalDataSource,
            messageLocalDataSource,
            userLocalDataSource,
            sharedPreferencesManager
        )
    }
}