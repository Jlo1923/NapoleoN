package com.naposystems.napoleonchat.di.modules

import android.content.Context
import com.naposystems.napoleonchat.db.dao.message.MessageDataSource
import com.naposystems.napoleonchat.db.dao.attachment.AttachmentDataSource
import com.naposystems.napoleonchat.db.dao.messageNotSent.MessageNotSentDataSource
import com.naposystems.napoleonchat.db.dao.quoteMessage.QuoteDataSource
import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.repository.conversation.ConversationRepository
import com.naposystems.napoleonchat.ui.conversation.IContractConversation
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.webService.NapoleonApi
import com.naposystems.napoleonchat.webService.socket.IContractSocketService
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
        quoteDataSource: QuoteDataSource,
        messageNotSent: MessageNotSentDataSource
    ): IContractConversation.Repository {
        return ConversationRepository(
            context,
            socketService,
            userLocalDataSource,
            messageLocalDataSource,
            attachmentDataSource,
            sharedPreferencesManager,
            napoleonApi,
            quoteDataSource,
            messageNotSent
        )
    }
}