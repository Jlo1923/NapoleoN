package com.naposystems.pepito.di.modules

import android.content.Context
import com.naposystems.pepito.repository.contacts.ContactsRepository
import com.naposystems.pepito.ui.contacts.IContractContacts
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ContactsModule {

    @Provides
    @Singleton
    fun provideRepository(context: Context): IContractContacts.Repository {
        return ContactsRepository(context)
    }
}