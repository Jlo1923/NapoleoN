package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.repository.contacts.ContactsRepository
import com.naposystems.pepito.ui.contacts.IContractContacts
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.webService.NapoleonApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ContactsModule {

    @Provides
    @Singleton
    fun provideRepository(
        napoleonApi: NapoleonApi,
        contactDataSource: ContactDataSource,
        sharedPreferencesManager: SharedPreferencesManager
    ): IContractContacts.Repository {
        return ContactsRepository(napoleonApi, contactDataSource, sharedPreferencesManager)
    }
}