package com.naposystems.pepito.db.dao.message

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.MessageAndAttachment

interface MessageDataSource {

    fun getMessages(contactId: Int, pageSize: Int): LiveData<PagedList<MessageAndAttachment>>

    fun insertMessage(message: Message): Long

    fun insertListMessage(messageList: List<Message>)

    fun updateMessage(message: Message)

    suspend fun updateStateSelectionMessage(idContact: Int, idMessage: Int, isSelected : Int)

    suspend fun cleanSelectionMessages(idContact: Int)

    suspend fun deleteMessagesSelected(idContact: Int, listMessages: List<MessageAndAttachment>)

    suspend fun copyMessagesSelected(idContact: Int): List<String>

    suspend fun getLastMessageByContact(idContact: Int): MessageAndAttachment

    suspend fun getIdContactWithWebId(ListWebId: List<String>): Int

    suspend fun getMessagesSelected(idContact: Int): LiveData<List<MessageAndAttachment>>

    suspend fun deletedMessages(listWebIdMessages: List<String>)

    fun updateMessageStatus(messagesWebIds: List<String>, /*selfDestructTime: Int,*/ status: Int)

    suspend fun getMessagesByStatus(contactId: Int, status: Int): List<String>

    suspend fun deleteMessages(idContact: Int)

    suspend fun setSelfDestructTimeByMessages(selfDestructTime: Int, contactId: Int)
}