package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.db.dao.contact.ContactDao
import com.naposystems.napoleonchat.repository.conversationMute.ConversationMuteRepository
import com.naposystems.napoleonchat.ui.muteConversation.IMuteConversation
import com.naposystems.napoleonchat.webService.NapoleonApi
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