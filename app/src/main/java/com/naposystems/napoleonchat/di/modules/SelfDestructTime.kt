package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.db.dao.contact.ContactDataSource
import com.naposystems.napoleonchat.db.dao.message.MessageDataSource
import com.naposystems.napoleonchat.repository.selfDestructTime.SelfDestructTimeRepository
import com.naposystems.napoleonchat.ui.selfDestructTime.IContractSelfDestructTime
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
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