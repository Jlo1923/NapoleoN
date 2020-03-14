package com.naposystems.pepito.di.modules

import android.content.Context
import com.naposystems.pepito.db.dao.message.MessageDataSource
import com.naposystems.pepito.db.dao.attachment.AttachmentDataSource
import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.db.dao.conversation.ConversationDataSource
import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.repository.conversation.ConversationRepository
import com.naposystems.pepito.ui.conversation.IContractConversation
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.webService.NapoleonApi
import com.naposystems.pepito.webService.socket.IContractSocketService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ConversationModule {

    @Provides
    @Singleton
    fun provideRepository(
        context: Context,
        socketService: IContractSocketService.SocketService,
        userLocalDataSource: UserLocalDataSource,
        messageLocalDataSource: MessageDataSource,
        attachmentDataSource: AttachmentDataSource,
        sharedPreferencesManager: SharedPreferencesManager,
        napoleonApi: NapoleonApi,
        conversationLocalDataSource: ConversationDataSource,
        contactDataSource: ContactDataSource
    ): IContractConversation.Repository {
        return ConversationRepository(
            context,
            socketService,
            userLocalDataSource,
            messageLocalDataSource,
            attachmentDataSource,
            sharedPreferencesManager,
            napoleonApi,
            conversationLocalDataSource,
            contactDataSource
        )
    }
}