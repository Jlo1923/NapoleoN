package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.repository.securitySettings.SecuritySettingsRepository
import com.naposystems.napoleonchat.ui.securitySettings.IContractSecuritySettings
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
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