package com.naposystems.pepito.di.modules

import com.naposystems.pepito.repository.colorScheme.ColorSchemeRepository
import com.naposystems.pepito.utility.SharedPreferencesManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ColorSchemeModule {

    @Provides
    @Singleton
    fun provideRepository(sharedPreferencesManager: SharedPreferencesManager): ColorSchemeRepository {
        return ColorSchemeRepository(sharedPreferencesManager)
    }
}