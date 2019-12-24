package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.conversation.ConversationDataSource
import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.repository.conversation.ConversationRepository
import com.naposystems.pepito.ui.conversation.IContractConversation
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.webService.NapoleonApi
import com.naposystems.pepito.webService.service.IContractSocketService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ConversationModule {

    @Provides
    @Singleton
    fun provideRepository(
        socketService: IContractSocketService,
        userLocalDataSource: UserLocalDataSource,
        conversationLocalDataSource: ConversationDataSource,
        sharedPreferencesManager: SharedPreferencesManager,
        napoleonApi: NapoleonApi
    ): IContractConversation.Repository {
        return ConversationRepository(
            socketService,
            userLocalDataSource,
            conversationLocalDataSource,
            sharedPreferencesManager,
            napoleonApi
        )
    }
}