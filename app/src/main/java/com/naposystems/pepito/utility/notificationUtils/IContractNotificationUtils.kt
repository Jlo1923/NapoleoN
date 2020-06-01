package com.naposystems.pepito.utility.notificationUtils

interface IContractNotificationUtils {

    interface Repository {
        fun notifyMessageReceived(messageId: String)
        fun getIsOnCallPref(): Boolean
    }
}