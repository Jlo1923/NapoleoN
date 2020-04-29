package com.naposystems.pepito.di.modules

import com.naposystems.pepito.repository.timeFormat.TimeFormatRepository
import com.naposystems.pepito.utility.SharedPreferencesManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class TimeFormatModule {
    @Provides
    @Singleton
    fun provideRepository(sharedPreferencesManager: SharedPreferencesManager): TimeFormatRepository {
        return TimeFormatRepository(sharedPreferencesManager)
    }
}