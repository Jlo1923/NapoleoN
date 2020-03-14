package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.db.dao.message.MessageDataSource
import com.naposystems.pepito.repository.selfDestructTime.SelfDestructTimeRepository
import com.naposystems.pepito.ui.selfDestructTime.IContractSelfDestructTime
import com.naposystems.pepito.utility.SharedPreferencesManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SelfDestructTime {

    @Provides
    @Singleton
    fun provideRepository(
        sharedPreferencesManager: SharedPreferencesManager,
        messageDao: MessageDataSource,
        contactDataSource: ContactDataSource
    ): IContractSelfDestructTime.Repository {
        return SelfDestructTimeRepository(sharedPreferencesManager, messageDao, contactDataSource)
    }
}