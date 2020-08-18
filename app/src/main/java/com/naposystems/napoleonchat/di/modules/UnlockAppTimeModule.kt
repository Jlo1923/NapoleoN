package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.repository.unlockAppTime.UnlockAppTimeRepository
import com.naposystems.napoleonchat.ui.unlockAppTime.IContractUnlockAppTime
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
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