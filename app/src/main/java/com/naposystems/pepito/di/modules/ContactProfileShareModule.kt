package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.repository.sharedRepository.ContactProfileShareRepository
import com.naposystems.pepito.utility.sharedViewModels.contactProfile.IContractContactProfileShare
import dagger.Module
import dagger.Provides

@Module
class ContactProfileShareModule {
    @Provides
    fun provideRepository(
        contactDataSource: ContactDataSource
    ): IContractContactProfileShare.Repository {
        return ContactProfileShareRepository(
            contactDataSource
        )
    }
}