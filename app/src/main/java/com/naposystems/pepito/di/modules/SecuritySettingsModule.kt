package com.naposystems.pepito.di.modules

import com.naposystems.pepito.repository.securitySettings.SecuritySettingsRepository
import com.naposystems.pepito.ui.securitySettings.IContractSecuritySettings
import com.naposystems.pepito.utility.SharedPreferencesManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SecuritySettingsModule {

    @Provides
    @Singleton
    fun provideRepository(sharedPreferencesManager: SharedPreferencesManager): IContractSecuritySettings.Repository {
        return SecuritySettingsRepository(sharedPreferencesManager)
    }
}