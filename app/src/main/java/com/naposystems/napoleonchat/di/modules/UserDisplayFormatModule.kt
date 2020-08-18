package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.repository.userDisplayFormat.UserDisplayFormatRepository
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
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