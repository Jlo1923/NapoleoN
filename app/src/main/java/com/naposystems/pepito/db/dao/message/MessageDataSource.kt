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

    fun updateMessageStatus(messagesWebIds: List<String>, status: Int)

    suspend fun getMessagesByStatus(status: Int): List<String>
}