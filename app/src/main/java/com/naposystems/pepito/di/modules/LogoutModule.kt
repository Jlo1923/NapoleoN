package com.naposystems.pepito.di.modules

import android.content.Context
import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.repository.logout.LogoutRepository
import com.naposystems.pepito.ui.logout.IContractLogout
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.webService.NapoleonApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class LogoutModule {

    @Provides
    @Singleton
    fun provideRepository(
        context: Context,
        napoleonApi: NapoleonApi,
        userLocalDataSource: UserLocalDataSource,
        sharedPreferencesManager: SharedPreferencesManager
    ): IContractLogout.Repository {
        return LogoutRepository(context, napoleonApi, userLocalDataSource, sharedPreferencesManager)
    }

}