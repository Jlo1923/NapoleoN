package com.naposystems.napoleonchat.di.modules

import android.content.Context
import com.naposystems.napoleonchat.db.dao.attachment.AttachmentDataSource
import com.naposystems.napoleonchat.db.dao.contact.ContactDataSource
import com.naposystems.napoleonchat.db.dao.message.MessageDataSource
import com.naposystems.napoleonchat.db.dao.quoteMessage.QuoteDataSource
import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.repository.home.HomeRepository
import com.naposystems.napoleonchat.ui.home.IContractHome
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.webService.NapoleonApi
import com.naposystems.napoleonchat.webService.socket.IContractSocketService
import com.naposystems.napoleonchat.webService.socket.SocketService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class HomeModule {

    @Provides
    @Singleton
    fun provideRepository(
        context: Context,
        napoleonApi: NapoleonApi,
        userLocalDataSource: UserLocalDataSource,
        sharedPreferencesManager: SharedPreferencesManager,
        socketService: IContractSocketService.SocketService,
        messageLocalDataSource: MessageDataSource,
        contactLocalDataSource: ContactDataSource,
        attachmentLocalDataSource: AttachmentDataSource,
        quoteDataSource: QuoteDataSource
    ): IContractHome.Repository {
        return HomeRepository(
            context,
            napoleonApi,
            userLocalDataSource,
            sharedPreferencesManager,
            socketService,
            messageLocalDataSource,
            contactLocalDataSource,
            attachmentLocalDataSource,
            quoteDataSource
        )
    }
}