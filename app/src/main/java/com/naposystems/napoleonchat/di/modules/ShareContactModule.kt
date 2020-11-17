package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.db.dao.contact.ContactDataSource
import com.naposystems.napoleonchat.db.dao.message.MessageLocalDataSource
import com.naposystems.napoleonchat.repository.sharedRepository.ShareContactRepository
import com.naposystems.napoleonchat.utility.sharedViewModels.contact.IContractShareContact
import com.naposystems.napoleonchat.webService.NapoleonApi
import dagger.Module
import dagger.Provides

@Module
class ShareContactModule {
     @Provides
     fun provideRepository(
         napoleonApi: NapoleonApi,
         contactDataSource: ContactDataSource,
         messageLocalDataSource: MessageLocalDataSource
     ): IContractShareContact.Repository {
        return ShareContactRepository(
            napoleonApi, contactDataSource, messageLocalDataSource
        )
     }
}