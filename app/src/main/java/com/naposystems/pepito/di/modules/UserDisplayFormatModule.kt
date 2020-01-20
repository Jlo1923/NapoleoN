package com.naposystems.pepito.di.modules

import com.naposystems.pepito.repository.userDisplayFormat.UserDisplayFormatRepository
import com.naposystems.pepito.utility.SharedPreferencesManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class UserDisplayFormatModule {

    @Provides
    @Singleton
    fun provideRepository(sharedPreferencesManager: SharedPreferencesManager): UserDisplayFormatRepository {
        return UserDisplayFormatRepository(sharedPreferencesManager)
    }
}