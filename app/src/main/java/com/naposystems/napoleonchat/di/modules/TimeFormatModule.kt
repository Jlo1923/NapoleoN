package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.repository.timeFormat.TimeFormatRepository
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
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