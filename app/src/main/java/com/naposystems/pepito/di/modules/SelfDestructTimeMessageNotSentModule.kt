package com.naposystems.pepito.di.modules

import com.naposystems.pepito.repository.selfDestructTimeMessageNotSent.SelfDestructTimeMessageNotSentRepository
import com.naposystems.pepito.ui.selfDestructTimeMessageNotSentFragment.IContractSelfDestructTimeMessageNotSent
import com.naposystems.pepito.utility.SharedPreferencesManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SelfDestructTimeMessageNotSentModule {
    @Provides
    @Singleton
    fun provideRepository(
        sharedPreferencesManager: SharedPreferencesManager
    ): IContractSelfDestructTimeMessageNotSent.Repository {
        return SelfDestructTimeMessageNotSentRepository(sharedPreferencesManager)
    }
}