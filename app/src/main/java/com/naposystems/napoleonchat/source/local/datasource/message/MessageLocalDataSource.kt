package com.naposystems.napoleonchat.source.local.datasource.message

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation

interface MessageLocalDataSource {

    suspend fun getMessageByWebId(webId: String, decrypt: Boolean): MessageAttachmentRelation?

    fun getMessages(contactId: Int): LiveData<List<MessageAttachmentRelation>>

    fun getQuoteId(quoteWebId: String): Int

    fun getLocalMessagesByStatus(contactId: Int, status: Int): List<MessageAttachmentRelation>

    suspend fun insertMessage(messageEntity: MessageEntity): Long

    fun insertListMessage(messageEntityList: List<MessageEntity>)

    fun updateMessage(messageEntity: MessageEntity)

    fun existMessage(id: String): Boolean

    suspend fun updateStateSelectionMessage(contactId: Int, idMessage: Int, isSelected: Int)

    suspend fun cleanSelectionMessages(contactId: Int)

    suspend fun deleteMessagesSelected(contactId: Int, listMessageRelations: List<MessageAttachmentRelation>)

    suspend fun deleteMessagesByStatusForMe(contactId: Int, status: Int)

    suspend fun copyMessagesSelected(contactId: Int): List<String>

    suspend fun getLastMessageByContact(contactId: Int): MessageAttachmentRelation

    suspend fun getIdContactWithWebId(listWebId: List<String>): Int

    suspend fun getMessagesSelected(contactId: Int): LiveData<List<MessageAttachmentRelation>>

    suspend fun deletedMessages(listWebIdMessages: List<String>)

    suspend fun updateMessageStatus(messagesWebIds: List<String>, status: Int)

    fun getMessagesForHome(): LiveData<List<MessageAttachmentRelation>>

    suspend fun getTextMessagesByStatus(contactId: Int, status: Int): List<MessageAttachmentRelation>

    suspend fun getMissedCallsByStatus(contactId: Int, status: Int): List<MessageAttachmentRelation>

    suspend fun deleteMessages(contactId: Int)

    suspend fun setSelfDestructTimeByMessages(selfDestructTime: Int, contactId: Int)

    fun verifyMessagesToDelete()

    suspend fun deleteMessageByType(contactId: Int, type: Int)

    suspend fun deleteDuplicatesMessages()
    suspend fun addUUID()
}