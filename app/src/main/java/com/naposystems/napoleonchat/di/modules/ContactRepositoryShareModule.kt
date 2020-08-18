package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.db.dao.contact.ContactDataSource
import com.naposystems.napoleonchat.db.dao.message.MessageDataSource
import com.naposystems.napoleonchat.repository.sharedRepository.ContactRepositoryShareRepository
import com.naposystems.napoleonchat.utility.sharedViewModels.contactRepository.IContractContactRepositoryShare
import com.naposystems.napoleonchat.webService.NapoleonApi
import dagger.Module
import dagger.Provides

@Module
class ContactRepositoryShareModule {
    @Provides
    fun provideRepository(
        napoleonApi: NapoleonApi,
        contactDataSource: ContactDataSource,
        messageLocalDataSource: MessageDataSource
    ): IContractContactRepositoryShare.Repository {
        return ContactRepositoryShareRepository(
            napoleonApi,
            contactDataSource,
            messageLocalDataSource
        )
    }
}