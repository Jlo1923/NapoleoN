package com.naposystems.napoleonchat.repository.notificationSettings

import com.naposystems.napoleonchat.ui.notificationSetting.IContractNotificationSetting
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import javax.inject.Inject

class NotificationSettingRepository @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractNotificationSetting.Repository {

    /*override fun getNotificationMessageChannelId(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_NOTIFICATION_MESSAGE_CHANNEL_ID
        )
    }*/
}