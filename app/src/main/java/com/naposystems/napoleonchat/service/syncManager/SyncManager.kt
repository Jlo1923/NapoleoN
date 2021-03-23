package com.naposystems.napoleonchat.service.syncManager

import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageDataEventRes
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageEventMessageRes

interface SyncManager {

    //region New Notification
    fun insertMessage(messageString: String)

    fun notifyMessageReceived(messageId: String)
    //endregion

    //region Socket Service

    //endregion

    fun getUserId(): Int

    fun getMyMessages(contactId: Int?)

    fun verifyMessagesReceived()

    fun verifyMessagesRead()

    fun insertNewMessage(newMessageDataEventRes: NewMessageDataEventRes)

    fun updateMessagesStatus(messagesWebIds: List<String>, state: Int)

    fun getDeletedMessages()

    fun deleteContact(contactId: Int?)

    fun rejectCall(contactId: Int, channel: String)

    fun existIdMessage(id: String): Boolean

    fun validateMessageType(messagesWebIds: List<String>, state: Int)
    //endregion

    //region Notification

//    fun notifyMessageReceived_NOTIF(messageId: String)
    fun getIsOnCallPref(): Boolean
//    fun getContactSilenced(contactId: Int, silenced: (Boolean?) -> Unit)
    fun getContact(contactId: Int): ContactEntity?
//    fun getNotificationChannelCreated(): Int
//    fun setNotificationChannelCreated()
//    fun getNotificationMessageChannelId(): Int
//    fun setNotificationMessageChannelId(newId: Int)
//    fun getCustomNotificationChannelId(contactId: Int): String?
//    fun setCustomNotificationChannelId(contactId: Int, newId: String)
//    fun getContactById(contactId: Int): ContactEntity?
//    fun updateStateChannel(contactId: Int, state: Boolean)
    //endegion

    suspend fun getRemoteContact()

//    suspend fun insertQuote_NOTIF(quoteWebId: String, messageId: Int)

    //    fun insertAttachments(listAttachments: List<AttachmentEntity>)

//    suspend fun NEW_insertMessage(newMessageEventMessageRes: NewMessageEventMessageRes)
//////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////
    fun callContact(contact: Int, videoCall: Boolean, offer: String)
}