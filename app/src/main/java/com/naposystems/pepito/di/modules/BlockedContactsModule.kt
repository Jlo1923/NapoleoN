package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.blockedContacts.BlockedContactsLocalDataSource
import com.naposystems.pepito.repository.blockedContact.BlockedContactRepository
import com.naposystems.pepito.webService.NapoleonApi
import dagger.Module
import dagger.Provides

@Module
class BlockedContactsModule {

    @Provides
    fun provideRepository(
        napoleonApi: NapoleonApi,
        blockedContactsLocalDataSource: BlockedContactsLocalDataSource
    ): BlockedContactRepository {
        return BlockedContactRepository(napoleonApi, blockedContactsLocalDataSource)
    }
}