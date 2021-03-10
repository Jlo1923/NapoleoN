package com.naposystems.napoleonchat.service.syncManager

import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageDataEventRes

interface SyncManager {

    //region SocketService
    fun getUserId(): Int

    fun getMyMessages(contactId: Int?)

    fun verifyMessagesReceived()

    fun verifyMessagesRead()

    fun insertNewMessage(newMessageDataEventRes: NewMessageDataEventRes)

    fun notifyMessageReceived(messageId: String)

    fun updateMessagesStatus(messagesWebIds: List<String>, state: Int)

    fun getDeletedMessages()

    fun deleteContact(contactId: Int?)

    fun rejectCall(contactId: Int, channel: String)

    fun existIdMessage(id: String): Boolean

    fun validateMessageType(messagesWebIds: List<String>, state: Int)
    //endregion

    //region Notification
    fun insertMessage(messageString: String)
    fun notifyMessageReceived_NOTIF(messageId: String)
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
    //endegion


}