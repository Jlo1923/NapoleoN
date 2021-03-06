package com.naposystems.napoleonchat.utility.notificationUtils

import com.naposystems.napoleonchat.source.local.entity.ContactEntity

interface IContractNotificationUtils {

    interface Repository {
        fun insertMessage(messageString: String)
        fun notifyMessageReceived(messageId: String)
        fun getIsOnCallPref(): Boolean
        fun getContactSilenced(contactId: Int, silenced : (Boolean?) -> Unit)
        fun getContact(contactId: Int): ContactEntity?
        fun getNotificationChannelCreated(): Int
        fun setNotificationChannelCreated()
        fun getNotificationMessageChannelId(): Int
        fun setNotificationMessageChannelId(newId:Int)
        fun getCustomNotificationChannelId(contactId: Int): String?
        fun setCustomNotificationChannelId(contactId: Int, newId: String)
        fun getContactById(contactId: Int): ContactEntity?
        fun updateStateChannel(contactId: Int, state:Boolean)
    }
}
