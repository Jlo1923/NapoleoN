package com.naposystems.pepito.di.modules

import android.content.Context
import com.naposystems.pepito.db.dao.attachment.AttachmentDataSource
import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.db.dao.message.MessageDataSource
import com.naposystems.pepito.db.dao.quoteMessage.QuoteDataSource
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
        context: Context,
        napoleonApi: NapoleonApi,
        userLocalDataSource: UserLocalDataSource,
        sharedPreferencesManager: SharedPreferencesManager,
        socketService: SocketService,
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