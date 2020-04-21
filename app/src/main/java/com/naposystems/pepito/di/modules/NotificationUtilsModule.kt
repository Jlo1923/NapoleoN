package com.naposystems.pepito.di.modules

import com.naposystems.pepito.repository.notificationUtils.NotificationUtilsRepository
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
        napoleonApi: NapoleonApi
    ): IContractNotificationUtils.Repository {
        return NotificationUtilsRepository(napoleonApi)
    }
}