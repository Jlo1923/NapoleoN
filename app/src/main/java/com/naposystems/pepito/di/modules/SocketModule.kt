package com.naposystems.pepito.di.modules

import android.content.Context
import com.naposystems.pepito.db.dao.attachment.AttachmentDataSource
import com.naposystems.pepito.db.dao.conversation.ConversationDataSource
import com.naposystems.pepito.db.dao.message.MessageDataSource
import com.naposystems.pepito.repository.socket.SocketRepository
import com.naposystems.pepito.webService.NapoleonApi
import com.naposystems.pepito.webService.socket.IContractSocketService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SocketModule {

    @Provides
    @Singleton
    fun provideRepository(
        context: Context,
        napoleonApi: NapoleonApi,
        conversationLocalDataSource: ConversationDataSource,
        messageLocalDataSource: MessageDataSource,
        attachmentLocalDataSource: AttachmentDataSource
    ): IContractSocketService.Repository {
        return SocketRepository(context, napoleonApi, conversationLocalDataSource, messageLocalDataSource, attachmentLocalDataSource)
    }
}