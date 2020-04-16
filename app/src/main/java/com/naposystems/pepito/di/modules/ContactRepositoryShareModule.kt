package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.repository.sharedRepository.ContactRepositoryShareRepository
import com.naposystems.pepito.utility.sharedViewModels.contactRepository.IContractContactRepositoryShare
import com.naposystems.pepito.webService.NapoleonApi
import dagger.Module
import dagger.Provides

@Module
class ContactRepositoryShareModule {
    @Provides
    fun provideRepository(
        napoleonApi: NapoleonApi,
        contactDataSource: ContactDataSource
    ): IContractContactRepositoryShare.Repository {
        return ContactRepositoryShareRepository(
            napoleonApi,
            contactDataSource
        )
    }
}