package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.contact.ContactDao
import com.naposystems.pepito.repository.conversationMute.ConversationMuteRepository
import com.naposystems.pepito.ui.muteConversation.IMuteConversation
import com.naposystems.pepito.webService.NapoleonApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class MuteConversationModule {

    @Provides
    @Singleton
    fun provideRepository(
        napoleonApi: NapoleonApi,
        contactDao: ContactDao
        ): IMuteConversation.Repository {
        return ConversationMuteRepository(napoleonApi, contactDao)
    }

}