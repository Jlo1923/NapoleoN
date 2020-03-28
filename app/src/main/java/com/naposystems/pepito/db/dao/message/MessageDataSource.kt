package com.naposystems.pepito.db.dao.message

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.MessageAndAttachment

interface MessageDataSource {

    fun getMessages(contactId: Int, pageSize: Int): LiveData<PagedList<MessageAndAttachment>>

    fun getLocalMessagesByStatus(contactId: Int, status: Int): List<MessageAndAttachment>

    fun insertMessage(message: Message): Long

    fun insertListMessage(messageList: List<Message>)

    fun updateMessage(message: Message)

    suspend fun updateStateSelectionMessage(contactId: Int, idMessage: Int, isSelected : Int)

    suspend fun cleanSelectionMessages(contactId: Int)

    suspend fun deleteMessagesSelected(contactId: Int, listMessages: List<MessageAndAttachment>)

    suspend fun deleteMessagesByStatusForMe(contactId: Int, status: Int)

    suspend fun copyMessagesSelected(contactId: Int): List<String>

    suspend fun getLastMessageByContact(contactId: Int): MessageAndAttachment

    suspend fun getIdContactWithWebId(listWebId: List<String>): Int

    suspend fun getMessagesSelected(contactId: Int): LiveData<List<MessageAndAttachment>>

    suspend fun deletedMessages(listWebIdMessages: List<String>)

    fun updateMessageStatus(messagesWebIds: List<String>, /*selfDestructTime: Int,*/ status: Int)

    suspend fun getMessagesByStatus(contactId: Int, status: Int): List<String>

    suspend fun deleteMessages(contactId: Int)

    suspend fun setSelfDestructTimeByMessages(selfDestructTime: Int, contactId: Int)
}