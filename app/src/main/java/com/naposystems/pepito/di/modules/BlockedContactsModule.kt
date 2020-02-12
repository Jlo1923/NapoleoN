package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.repository.blockedContact.BlockedContactRepository
import com.naposystems.pepito.webService.NapoleonApi
import dagger.Module
import dagger.Provides

@Module
class BlockedContactsModule {

    @Provides
    fun provideRepository(
        napoleonApi: NapoleonApi,
        contactsLocalDataSource: ContactDataSource
    ): BlockedContactRepository {
        return BlockedContactRepository(napoleonApi, contactsLocalDataSource)
    }
}