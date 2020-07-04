package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.contact.ContactLocalDataSource
import com.naposystems.pepito.repository.notificationUtils.NotificationUtilsRepository
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.utility.notificationUtils.IContractNotificationUtils
import com.naposystems.pepito.webService.NapoleonApi
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