package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.db.dao.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.repository.notificationUtils.NotificationUtilsRepository
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.notificationUtils.IContractNotificationUtils
import com.naposystems.napoleonchat.webService.NapoleonApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class NotificationUtilsModule {

    @Provides
    @Singleton
    fun provideRepository(
        napoleonApi: NapoleonApi,
        contactLocalDataSource: ContactLocalDataSource,
        sharedPreferencesManager: SharedPreferencesManager
    ): IContractNotificationUtils.Repository {
        return NotificationUtilsRepository(napoleonApi, contactLocalDataSource, sharedPreferencesManager)
    }
}