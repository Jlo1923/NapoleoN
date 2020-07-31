package com.naposystems.pepito.di.modules

import android.content.Context
import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.repository.cancelSubscription.CancelSubscriptionRepository
import com.naposystems.pepito.repository.logout.LogoutRepository
import com.naposystems.pepito.ui.cancelSubscription.IContractCancelSubscription
import com.naposystems.pepito.ui.logout.IContractLogout
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.webService.NapoleonApi
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