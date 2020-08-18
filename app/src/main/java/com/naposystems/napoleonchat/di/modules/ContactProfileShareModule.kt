package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.db.dao.contact.ContactDataSource
import com.naposystems.napoleonchat.repository.sharedRepository.ContactProfileShareRepository
import com.naposystems.napoleonchat.utility.sharedViewModels.contactProfile.IContractContactProfileShare
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