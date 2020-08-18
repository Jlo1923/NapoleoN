package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.db.dao.contact.ContactDataSource
import com.naposystems.napoleonchat.repository.contacts.ContactsRepository
import com.naposystems.napoleonchat.ui.contacts.IContractContacts
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