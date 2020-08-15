package com.naposystems.napoleonchat.utility.notificationUtils

interface IContractNotificationUtils {

    interface Repository {
        fun notifyMessageReceived(messageId: String)
        fun getIsOnCallPref(): Boolean
        fun getContactSilenced(contactId: Int, silenced : (Boolean?) -> Unit)
    }
}