package com.naposystems.napoleonchat.service.syncManager

import com.naposystems.napoleonchat.service.socketClient.GetMessagesSocketListener
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesReqDTO
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageDataEventRes

interface SyncManager {

    fun insertMessage(messageString: String)

    fun notifyMessageReceived(messageId: MessagesReqDTO)

    fun getUserId(): Int

    fun getMyMessages(contactId: Int?)

    fun verifyMessagesReceived()

    fun verifyMessagesRead()

    fun insertNewMessage(newMessageDataEventRes: NewMessageDataEventRes)

    fun updateMessagesStatus(messagesWebIds: List<String>, state: Int)

    fun updateAttachmentsStatus(attachmentsWebIds: List<String>, status: Int)

    fun getDeletedMessages()

    fun deleteContact(contactId: Int?)

    fun rejectCall(contactId: Int, channel: String)

    fun existMessageById(id: String): Boolean

    fun validateMessageType(messagesWebIds: List<String>, state: Int)

    fun getContact(contactId: Int): ContactEntity?

    suspend fun getRemoteContact()

    fun callContact(contact: Int, videoCall: Boolean, offer: String)

    fun existAttachmentById(it: String): Boolean


    fun setGetMessagesSocketListener(getMessagesSocketListener: GetMessagesSocketListener)
}