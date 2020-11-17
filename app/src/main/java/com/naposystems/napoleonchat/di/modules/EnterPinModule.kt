package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.repository.enterPin.EnterPinRepository
import com.naposystems.napoleonchat.ui.enterPin.IContractEnterPin
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class EnterPinModule {
    @Provides
    @Singleton
    fun provideRepository(
        sharedPreferencesManager: SharedPreferencesManager,
        userLocalDataSource: UserLocalDataSource
    ): IContractEnterPin.Repository {
        return EnterPinRepository(sharedPreferencesManager, userLocalDataSource)
    }
}