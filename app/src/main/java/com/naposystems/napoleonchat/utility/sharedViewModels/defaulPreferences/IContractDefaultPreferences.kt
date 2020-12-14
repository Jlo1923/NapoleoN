package com.naposystems.napoleonchat.utility.sharedViewModels.defaulPreferences

interface IContractDefaultPreferences {

    interface ViewModel {
        fun setDefaultPreferences()
    }

    interface Repository {
        suspend fun setDefaultTheme()
        suspend fun setDefaultUserDisplayFormat()
        suspend fun setDefaultTimeFormat()
        suspend fun setDefaultSelfDestructTime()
        suspend fun setDefaultTimeRequestAccessPin()
        suspend fun setDefaultAllowDownloadAttachments()
        suspend fun setDefaultLockType()
        suspend fun setDefaultSelfDestructTimeMessageNotSent()
        suspend fun setDefaultAttemptsForRetryCode()
        suspend fun setDefaultTimeForRetryCode()
        suspend fun setDefaultAttemptsForNewCode()
        suspend fun setDefaultNotificationMessageChannelId()
        suspend fun setDefaultNotificationGroupChannelId()
        suspend fun setDefaultDialogSubscription()
    }

}