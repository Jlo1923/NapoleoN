package com.naposystems.napoleonchat.utility.notificationUtils

import com.naposystems.napoleonchat.dto.newMessageEvent.NewMessageEventMessageRes
import com.naposystems.napoleonchat.entity.Contact

interface IContractNotificationUtils {

    interface Repository {
        fun insertMessage(message: NewMessageEventMessageRes)
        fun notifyMessageReceived(messageId: String)
        fun getIsOnCallPref(): Boolean
        fun getContactSilenced(contactId: Int, silenced : (Boolean?) -> Unit)
        fun getContact(contactId: Int): Contact?
    }
}