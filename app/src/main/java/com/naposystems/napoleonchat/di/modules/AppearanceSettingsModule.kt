package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.repository.appearanceSettings.AppearanceSettingsRepository
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppearanceSettingsModule {

    @Provides
    @Singleton
    fun provideRepository(
        sharedPreferencesManager: SharedPreferencesManager,
        userLocalDataSource: UserLocalDataSource
    ): AppearanceSettingsRepository {
        return AppearanceSettingsRepository(sharedPreferencesManager, userLocalDataSource)
    }
}