package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.repository.contactProfile.ContactProfileRepository
import com.naposystems.pepito.ui.contactProfile.IContractContactProfile
import com.naposystems.pepito.webService.NapoleonApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ContactProfileModule {

    @Provides
    @Singleton
    fun provideRepository(
        napoleonApi: NapoleonApi,
        contactDataSource: ContactDataSource
    ): IContractContactProfile.Repository {
        return ContactProfileRepository(napoleonApi, contactDataSource)
    }

}
