package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.db.dao.conversation.ConversationLocalDataSource
import com.naposystems.pepito.db.dao.message.MessageLocalDataSource
import com.naposystems.pepito.repository.sharedRepository.ShareContactRepository
import com.naposystems.pepito.utility.sharedViewModels.contact.IContractShareContact
import com.naposystems.pepito.webService.NapoleonApi
import dagger.Module
import dagger.Provides

@Module
class ShareContactModule {
     @Provides
     fun provideRepository(
         napoleonApi: NapoleonApi,
         contactDataSource: ContactDataSource,
         conversationLocalDataSource: ConversationLocalDataSource,
         messageLocalDataSource: MessageLocalDataSource
     ): IContractShareContact.Repository {
        return ShareContactRepository(
            napoleonApi, contactDataSource, conversationLocalDataSource, messageLocalDataSource
        )
     }
}