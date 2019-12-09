package com.naposystems.pepito.di.modules

import com.naposystems.pepito.repository.appearanceSettings.AppearanceSettingsRepository
import com.naposystems.pepito.utility.SharedPreferencesManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppearanceSettingsModule {

    @Provides
    @Singleton
    fun provideRepository(sharedPreferencesManager: SharedPreferencesManager): AppearanceSettingsRepository {
        return AppearanceSettingsRepository(sharedPreferencesManager)
    }
}