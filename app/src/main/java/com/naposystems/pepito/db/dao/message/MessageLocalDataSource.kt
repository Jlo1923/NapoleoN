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

    override fun getMessageByWebId(webId: String) = messageDao.getMessageByWebId(webId)

    override fun getMessages(contactId: Int): LiveData<List<MessageAndAttachment>> {
        return messageDao.getMessagesAndAttachments(contactId)
    }

    override fun getQuoteId(quoteWebId: String): Int {
        return messageDao.getQuoteId(quoteWebId)
    }

    override fun getLocalMessagesByStatus(contactId: Int, status: Int): List<MessageAndAttachment> {
        return messageDao.getLocalMessagesByStatus(contactId, status)
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

    override suspend fun updateStateSelectionMessage(contactId: Int, idMessage: Int, isSelected : Int) {
        messageDao.updateMessagesSelected(contactId, idMessage, isSelected)
    }

    override suspend fun cleanSelectionMessages(contactId: Int) {
        messageDao.cleanSelectionMessages(contactId)
    }

    override suspend fun deleteMessagesSelected(contactId: Int, listMessages: List<MessageAndAttachment>) {
        listMessages.forEach {messageAndAttachment ->
            messageDao.deleteMessagesSelected(contactId, messageAndAttachment.message.id)
        }
    }

    override suspend fun deleteMessagesByStatusForMe(contactId: Int, status: Int) {
        messageDao.deleteMessagesByStatusForMe(contactId, status)
    }

    override suspend fun getLastMessageByContact(contactId: Int): MessageAndAttachment {
        return messageDao.getLastMessageByContact(contactId)
    }

    override suspend fun copyMessagesSelected(contactId: Int) : List<String> {
        return messageDao.copyMessagesSelected(contactId)
    }

    override suspend fun getMessagesSelected(contactId: Int): LiveData<List<MessageAndAttachment>> {
        return messageDao.getMessagesSelected(contactId)
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

    override suspend fun deleteMessages(contactId: Int) {
        messageDao.deleteMessages(contactId)
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