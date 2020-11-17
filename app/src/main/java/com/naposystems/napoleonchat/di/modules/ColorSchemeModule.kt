package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.repository.colorScheme.ColorSchemeRepository
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
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