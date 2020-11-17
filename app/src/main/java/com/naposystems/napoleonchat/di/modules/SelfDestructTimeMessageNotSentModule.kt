package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.repository.selfDestructTimeMessageNotSent.SelfDestructTimeMessageNotSentRepository
import com.naposystems.napoleonchat.ui.selfDestructTimeMessageNotSentFragment.IContractSelfDestructTimeMessageNotSent
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
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