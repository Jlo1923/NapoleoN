package com.naposystems.napoleonchat.di.modules

import android.content.Context
import com.naposystems.napoleonchat.db.dao.attachment.AttachmentDataSource
import com.naposystems.napoleonchat.db.dao.contact.ContactDataSource
import com.naposystems.napoleonchat.db.dao.message.MessageDataSource
import com.naposystems.napoleonchat.db.dao.quoteMessage.QuoteDataSource
import com.naposystems.napoleonchat.repository.socket.SocketRepository
import com.naposystems.napoleonchat.webService.NapoleonApi
import com.naposystems.napoleonchat.webService.socket.IContractSocketService
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
        messageLocalDataSource: MessageDataSource,
        attachmentLocalDataSource: AttachmentDataSource,
        quoteDataSource: QuoteDataSource,
        contactDataSource: ContactDataSource
    ): IContractSocketService.Repository {
        return SocketRepository(
            context,
            napoleonApi,
            messageLocalDataSource,
            attachmentLocalDataSource,
            quoteDataSource,
            contactDataSource
        )
    }
}