package com.naposystems.napoleonchat.di.modules

import android.content.Context
import com.naposystems.napoleonchat.db.dao.attachment.AttachmentDataSource
import com.naposystems.napoleonchat.db.dao.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.db.dao.message.MessageDataSource
import com.naposystems.napoleonchat.db.dao.quoteMessage.QuoteDataSource
import com.naposystems.napoleonchat.repository.notificationUtils.NotificationUtilsRepository
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.notificationUtils.IContractNotificationUtils
import com.naposystems.napoleonchat.webService.NapoleonApi
import com.naposystems.napoleonchat.webService.socket.IContractSocketService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class NotificationUtilsModule {

    @Provides
    @Singleton
    fun provideRepository(
        context: Context,
        napoleonApi: NapoleonApi,
        socketService: IContractSocketService.SocketService,
        contactLocalDataSource: ContactLocalDataSource,
        messageLocalDataSource: MessageDataSource,
        quoteDataSource: QuoteDataSource,
        attachmentLocalDataSource: AttachmentDataSource,
        sharedPreferencesManager: SharedPreferencesManager
    ): IContractNotificationUtils.Repository {
        return NotificationUtilsRepository(
            context,
            napoleonApi,
            socketService,
            contactLocalDataSource,
            messageLocalDataSource,
            quoteDataSource,
            attachmentLocalDataSource,
            sharedPreferencesManager,
        )
    }
}