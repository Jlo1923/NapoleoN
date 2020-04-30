package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.repository.conversationCall.ConversationCallRepository
import com.naposystems.pepito.ui.conversationCall.IContractConversationCall
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.webService.NapoleonApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ConversationCallModule {

    @Provides
    @Singleton
    fun provideRepository(
        napoleonApi: NapoleonApi,
        contactDataSource: ContactDataSource,
        sharedPreferencesManager: SharedPreferencesManager
    ): IContractConversationCall.Repository =
        ConversationCallRepository(napoleonApi, contactDataSource, sharedPreferencesManager)
}