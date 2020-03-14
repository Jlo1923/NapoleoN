package com.naposystems.pepito.db.dao.message

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Utils
import java.util.concurrent.TimeUnit
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

    override suspend fun updateStateSelectionMessage(idContact: Int, idMessage: Int, isSelected : Int) {
        messageDao.updateMessagesSelected(idContact, idMessage, isSelected)
    }

    override suspend fun cleanSelectionMessages(idContact: Int) {
        messageDao.cleanSelectionMessages(idContact)
    }

    override suspend fun deleteMessagesSelected(idContact: Int, listMessages: List<MessageAndAttachment>) {
        listMessages.forEach {messageAndAttachment ->
            messageDao.deleteMessagesSelected(idContact, messageAndAttachment.message.id)
        }
    }

    override suspend fun getLastMessageByContact(idContact: Int): MessageAndAttachment {
        return messageDao.getLastMessageByContact(idContact)
    }

    override suspend fun copyMessagesSelected(idContact: Int) : List<String> {
        return messageDao.copyMessagesSelected(idContact)
    }

    override suspend fun getMessagesSelected(idContact: Int): LiveData<List<MessageAndAttachment>> {
        return messageDao.getMessagesSelected(idContact)
    }

    override fun updateMessageStatus(messagesWebIds: List<String>, status: Int) {
        messagesWebIds.forEach {messageWebId ->
            when(status) {
                Constants.MessageStatus.READED.status -> {
                    val timeByMessage = messageDao.getSelfDestructTimeByMessage(messageWebId)
                    val currentTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
                    val time = currentTime.plus(Utils.convertItemOfTimeInSeconds(timeByMessage))

                    messageDao.updateMessageStatus(messageWebId, currentTime, time, status)
                }
                else -> {
                    messageDao.updateMessageStatus(messageWebId, 0, 0, status)
                }
            }
        }
    }

    override suspend fun getMessagesByStatus(contactId: Int, status: Int): List<String> {
        return messageDao.getMessagesByStatus(contactId, status)
    }

    override suspend fun deleteMessages(idContact: Int) {
        messageDao.deleteMessages(idContact)
    }

    override suspend fun setSelfDestructTimeByMessages(selfDestructTime: Int, contactId: Int) {
        messageDao.setSelfDestructTimeByMessages(selfDestructTime, contactId)
    }

    override suspend fun deletedMessages(listWebIdMessages: List<String>) {
        listWebIdMessages.forEach {webId ->
            messageDao.deletedMessages(webId)
        }
    }

    override suspend fun getIdContactWithWebId(listWebId: List<String>): Int {
        return messageDao.getIdContactWithWebId(listWebId[0])
    }
}