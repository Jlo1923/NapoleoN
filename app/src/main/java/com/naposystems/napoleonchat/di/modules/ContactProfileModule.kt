package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.db.dao.contact.ContactDataSource
import com.naposystems.napoleonchat.repository.contactProfile.ContactProfileRepository
import com.naposystems.napoleonchat.ui.contactProfile.IContractContactProfile
import com.naposystems.napoleonchat.webService.NapoleonApi
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
