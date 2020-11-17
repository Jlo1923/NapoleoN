package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.repository.subscription.SubscriptionRepository
import com.naposystems.napoleonchat.ui.subscription.IContractSubscription
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.webService.NapoleonApi
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