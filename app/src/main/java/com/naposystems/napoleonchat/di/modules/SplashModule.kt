package com.naposystems.napoleonchat.di.modules

import android.content.Context
import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.repository.splash.SplashRepository
import com.naposystems.napoleonchat.ui.splash.IContractSplash
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import dagger.Module
import dagger.Provides

@Module
class SplashModule {

    @Provides
    fun provideRepository(
        context: Context,
        sharedPreferencesManager: SharedPreferencesManager,
        userDataSource: UserLocalDataSource
    ): IContractSplash.Repository {
        return SplashRepository(context, sharedPreferencesManager, userDataSource)
    }
}