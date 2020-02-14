package com.naposystems.pepito.di.modules

import com.naposystems.pepito.repository.base.BaseRepository
import com.naposystems.pepito.ui.baseFragment.IContractBase
import com.naposystems.pepito.utility.SharedPreferencesManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class BaseModule {
    @Provides
    @Singleton
    fun provideRepository(
        sharedPreferencesManager: SharedPreferencesManager): IContractBase.Repository {
        return BaseRepository(sharedPreferencesManager)
    }
}