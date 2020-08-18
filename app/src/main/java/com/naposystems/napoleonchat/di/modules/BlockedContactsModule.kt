package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.db.dao.contact.ContactDataSource
import com.naposystems.napoleonchat.repository.blockedContact.BlockedContactRepository
import com.naposystems.napoleonchat.webService.NapoleonApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class BlockedContactsModule {

    @Provides
    @Singleton
    fun provideRepository(
        napoleonApi: NapoleonApi,
        contactsLocalDataSource: ContactDataSource
    ): BlockedContactRepository {
        return BlockedContactRepository(napoleonApi, contactsLocalDataSource)
    }
}