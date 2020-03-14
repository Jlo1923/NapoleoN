package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.repository.status.StatusRepository
import com.naposystems.pepito.repository.subscription.SubscriptionRepository
import com.naposystems.pepito.ui.subscription.IContractSubscription
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.webService.NapoleonApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SubscriptionModule {

    @Provides
    @Singleton
    fun provideRepository(
        napoleonApi: NapoleonApi,
        sharedPreferencesManager: SharedPreferencesManager
    ): IContractSubscription.Repository {
        return SubscriptionRepository(napoleonApi, sharedPreferencesManager)
    }
}