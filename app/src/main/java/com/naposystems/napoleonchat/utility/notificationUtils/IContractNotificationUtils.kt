package com.naposystems.napoleonchat.utility.notificationUtils

import com.naposystems.napoleonchat.entity.Contact

interface IContractNotificationUtils {

    interface Repository {
        fun insertMessage(messageString: String)
        fun notifyMessageReceived(messageId: String)
        fun getIsOnCallPref(): Boolean
        fun getContactSilenced(contactId: Int, silenced : (Boolean?) -> Unit)
        fun getContact(contactId: Int): Contact?
        fun getNotificationChannelCreated(): Int
        fun setNotificationChannelCreated()
        fun getNotificationMessageChannelId(): Int
        fun setNotificationMessageChannelId(newId:Int)
    }
}
