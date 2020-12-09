package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.repository.notificationSettings.NotificationSettingRepository
import com.naposystems.napoleonchat.ui.notifications.IContractNotificationSetting
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class NotificationSettingModule {
    @Provides
    @Singleton
    fun provideRepository(
        sharedPreferencesManager: SharedPreferencesManager
    ): IContractNotificationSetting.Repository {
        return NotificationSettingRepository(sharedPreferencesManager)
    }
}