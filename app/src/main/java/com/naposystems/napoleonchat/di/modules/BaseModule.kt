package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.repository.base.BaseRepository
import com.naposystems.napoleonchat.ui.baseFragment.IContractBase
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
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