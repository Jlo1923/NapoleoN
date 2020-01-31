package com.naposystems.pepito.di.modules

import com.naposystems.pepito.repository.unlockAppTime.UnlockAppTimeRepository
import com.naposystems.pepito.ui.unlockAppTime.IContractUnlockAppTime
import com.naposystems.pepito.utility.SharedPreferencesManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class UnlockAppTimeModule {
    @Provides
    @Singleton
    fun provideRepository(sharedPreferencesManager: SharedPreferencesManager): IContractUnlockAppTime.Repository {
        return UnlockAppTimeRepository(sharedPreferencesManager)
    }
}