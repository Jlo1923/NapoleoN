package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.db.dao.contact.ContactLocalDataSource
import com.naposystems.pepito.repository.addContact.AddContactRepository
import com.naposystems.pepito.ui.addContact.IContractAddContact
import com.naposystems.pepito.webService.NapoleonApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AddContactModule {

    @Provides
    @Singleton
    fun provideRepository(
        napoleonApi: NapoleonApi,
        contactLocalDataSource: ContactDataSource
    ): IContractAddContact.Repository {
        return AddContactRepository(napoleonApi, contactLocalDataSource)
    }
}