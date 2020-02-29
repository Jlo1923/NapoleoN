package com.naposystems.pepito.db.dao.message

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.MessageAndAttachment
import javax.inject.Inject

class MessageLocalDataSource @Inject constructor(
    private val messageDao: MessageDao
) : MessageDataSource {

    override fun getMessages(
        contactId: Int,
        pageSize: Int
    ): LiveData<PagedList<MessageAndAttachment>> {

        val pagedListConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setInitialLoadSizeHint(10)
            .setPageSize(10 * 2)
            .build()

        val dataSourceFactory = messageDao.getMessagesAndAttachments(contactId)

        return LivePagedListBuilder(
            dataSourceFactory, pagedListConfig
        ).build()
    }

    override fun insertMessage(message: Message): Long {
        return messageDao.insertMessage(message)
    }

    override fun insertListMessage(messageList: List<Message>) {
        messageDao.insertMessageList(messageList)
    }

    override fun updateMessage(message: Message) {
        messageDao.updateMessage(message)
    }

    override fun updateMessageStatus(messagesWebIds: List<String>, status: Int) {
        for (messageWebId in messagesWebIds) {
            messageDao.updateMessageStatus(messageWebId, status)
        }
    }

    override suspend fun getMessagesByStatus(contactId: Int, status: Int): List<String> {
        return messageDao.getMessagesByStatus(contactId, status)
    }

    override suspend fun deleteMessages(idContact: Int) {
        messageDao.deleteMessages(idContact)
    }
}