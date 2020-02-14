package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.attachment.AttachmentDataSource
import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.db.dao.conversation.ConversationDataSource
import com.naposystems.pepito.db.dao.message.MessageDataSource
import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.repository.home.HomeRepository
import com.naposystems.pepito.ui.home.IContractHome
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.webService.NapoleonApi
import com.naposystems.pepito.webService.socket.SocketService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class HomeModule {

    @Provides
    @Singleton
    fun provideRepository(
        napoleonApi: NapoleonApi,
        userLocalDataSource: UserLocalDataSource,
        sharedPreferencesManager: SharedPreferencesManager,
        socketService: SocketService,
        conversationLocalDataSource: ConversationDataSource,
        messageLocalDataSource: MessageDataSource,
        contactLocalDataSource: ContactDataSource,
        attachmentLocalDataSource: AttachmentDataSource
    ): IContractHome.Repository {
        return HomeRepository(
            napoleonApi,
            userLocalDataSource,
            sharedPreferencesManager,
            socketService,
            conversationLocalDataSource,
            messageLocalDataSource,
            contactLocalDataSource,
            attachmentLocalDataSource
        )
    }
}