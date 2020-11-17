package com.naposystems.napoleonchat.di.modules

import android.content.Context
import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.repository.cancelSubscription.CancelSubscriptionRepository
import com.naposystems.napoleonchat.ui.cancelSubscription.IContractCancelSubscription
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.webService.NapoleonApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class CancelSubscriptionModule {

    @Provides
    @Singleton
    fun provideRepository(
        context: Context,
        napoleonApi: NapoleonApi,
        userLocalDataSource: UserLocalDataSource,
        sharedPreferencesManager: SharedPreferencesManager
    ): IContractCancelSubscription.Repository {
        return CancelSubscriptionRepository(
            context,
            napoleonApi,
            userLocalDataSource,
            sharedPreferencesManager
        )
    }

}