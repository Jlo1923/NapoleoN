package com.naposystems.pepito.di.modules

import com.naposystems.pepito.repository.selfDestructTime.SelfDestructTimeRepository
import com.naposystems.pepito.ui.selfDestructTime.IContractSelfDestructTime
import com.naposystems.pepito.utility.SharedPreferencesManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SelfDestructTime {

    @Provides
    @Singleton
    fun provideRepository(sharedPreferencesManager: SharedPreferencesManager): IContractSelfDestructTime.Repository {
        return SelfDestructTimeRepository(sharedPreferencesManager)
    }
}