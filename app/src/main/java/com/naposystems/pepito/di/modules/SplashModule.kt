package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.repository.splash.SplashRepository
import com.naposystems.pepito.ui.splash.IContractSplash
import com.naposystems.pepito.utility.SharedPreferencesManager
import dagger.Module
import dagger.Provides

@Module
class SplashModule {

    @Provides
    fun provideRepository(
        sharedPreferencesManager: SharedPreferencesManager,
        userDataSource: UserLocalDataSource
    ): IContractSplash.Repository {
        return SplashRepository(sharedPreferencesManager, userDataSource)
    }
}