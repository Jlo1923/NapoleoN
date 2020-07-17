package com.naposystems.pepito.db.dao.message

import androidx.lifecycle.LiveData
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.MessageAndAttachment
import kotlinx.coroutines.flow.Flow

interface MessageDataSource {

    suspend fun getMessageByWebId(webId: String, decrypt: Boolean): MessageAndAttachment?

    fun getMessages(contactId: Int): LiveData<List<MessageAndAttachment>>

    fun getQuoteId(quoteWebId: String): Int

    fun getLocalMessagesByStatus(contactId: Int, status: Int): List<MessageAndAttachment>

    fun insertMessage(message: Message): Long

    fun insertListMessage(messageList: List<Message>)

    fun updateMessage(message: Message)

    suspend fun updateStateSelectionMessage(contactId: Int, idMessage: Int, isSelected: Int)

    suspend fun cleanSelectionMessages(contactId: Int)

    suspend fun deleteMessagesSelected(contactId: Int, listMessages: List<MessageAndAttachment>)

    suspend fun deleteMessagesByStatusForMe(contactId: Int, status: Int)

    suspend fun copyMessagesSelected(contactId: Int): List<String>

    suspend fun getLastMessageByContact(contactId: Int): MessageAndAttachment

    suspend fun getIdContactWithWebId(listWebId: List<String>): Int

    suspend fun getMessagesSelected(contactId: Int): LiveData<List<MessageAndAttachment>>

    suspend fun deletedMessages(listWebIdMessages: List<String>)

    suspend fun updateMessageStatus(messagesWebIds: List<String>, status: Int)

    fun getMessagesForHome(): LiveData<List<MessageAndAttachment>>

    suspend fun getTextMessagesByStatus(contactId: Int, status: Int): List<MessageAndAttachment>

    suspend fun getMissedCallsByStatus(contactId: Int, status: Int): List<MessageAndAttachment>

    suspend fun deleteMessages(contactId: Int)

    suspend fun setSelfDestructTimeByMessages(selfDestructTime: Int, contactId: Int)

    fun verifyMessagesToDelete()

    suspend fun deleteMessageByType(contactId: Int, type: Int)
}