package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.attachment.AttachmentDataSource
import com.naposystems.pepito.db.dao.message.MessageDataSource
import com.naposystems.pepito.db.dao.quoteMessage.QuoteDataSource
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
        napoleonApi: NapoleonApi,
        messageLocalDataSource: MessageDataSource,
        attachmentLocalDataSource: AttachmentDataSource,
        quoteDataSource: QuoteDataSource
    ): IContractSocketService.Repository {
        return SocketRepository(
            napoleonApi,
            messageLocalDataSource,
            attachmentLocalDataSource,
            quoteDataSource
        )
    }
}