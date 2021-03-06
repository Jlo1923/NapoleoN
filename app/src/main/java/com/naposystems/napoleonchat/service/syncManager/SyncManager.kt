package com.naposystems.napoleonchat.service.syncManager

import com.naposystems.napoleonchat.service.socketClient.GetMessagesSocketListener
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesReqDTO
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageDataEventRes

interface SyncManager {

    fun insertMessage(messageString: String)

    fun notifyMessageReceivedRemote(messageId: MessagesReqDTO)

    fun notifyMessagesReaded()

    fun getUserId(): Int

    fun getMyMessages(contactId: Int?)

    fun verifyMessagesReceived()

    fun verifyMessagesRead()

    fun insertNewMessage(newMessageDataEventRes: NewMessageDataEventRes)

    fun updateMessagesStatus(
        messagesWebIds: List<String>,
        state: Int
    )

    fun updateAttachmentsStatus(attachmentsWebIds: List<String>, status: Int)

    fun getDeletedMessages()

    fun deleteContact(contactId: Int?)

    fun sendMissedCall()

    fun rejectCall(contactId: Int, channelName: String)

    fun rejectCall()

    fun cancelCall()

    fun existMessageById(id: String): Boolean

    fun validateMessageType(messagesWebIds: List<String>, state: Int)

    fun getContact(contactId: Int): ContactEntity?

    suspend fun getRemoteContact()

    fun callContact()

    fun existAttachmentById(it: String): Boolean

    fun setGetMessagesSocketListener(getMessagesSocketListener: GetMessagesSocketListener)

    fun tryMarkMessageParentAsReceived(idsAttachments: List<String>)

    fun tryMarkMessageParentAsRead(idsAttachments: List<String>)
}