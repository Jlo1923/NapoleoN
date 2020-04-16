package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.repository.contacts.ContactsRepository
import com.naposystems.pepito.ui.contacts.IContractContacts
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ContactsModule {

    @Provides
    @Singleton
    fun provideRepository(
        contactDataSource: ContactDataSource
    ): IContractContacts.Repository {
        return ContactsRepository(contactDataSource)
    }
}