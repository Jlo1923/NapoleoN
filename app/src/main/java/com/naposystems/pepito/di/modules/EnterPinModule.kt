package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.repository.enterPin.EnterPinRepository
import com.naposystems.pepito.ui.enterPin.IContractEnterPin
import com.naposystems.pepito.utility.SharedPreferencesManager
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