package com.naposystems.pepito.db.dao.message

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.naposystems.pepito.BuildConfig
import com.naposystems.pepito.crypto.message.CryptoMessage
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Utils
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MessageLocalDataSource @Inject constructor(
    private val context: Context,
    private val messageDao: MessageDao
) : MessageDataSource {

    val cryptoMessage = CryptoMessage(context)

    override suspend fun getMessageByWebId(webId: String, decrypt: Boolean): MessageAndAttachment? {
        val messageAndAttachment = messageDao.getMessageByWebId(webId)
        if (BuildConfig.ENCRYPT_API && decrypt) {
            with(messageAndAttachment?.message) {
                this?.let {
                    it.body = it.getBody(cryptoMessage)
                }
            }
        }

        return messageAndAttachment
    }

    override fun getMessages(contactId: Int) =
        messageDao.getMessagesAndAttachmentsDistinctUntilChanged(contactId)
            .map { listMessages: List<MessageAndAttachment> ->
                if (BuildConfig.ENCRYPT_API) {
                    listMessages.forEach { messageAndAttachment: MessageAndAttachment ->
                        with(messageAndAttachment.message) {
                            this.let {
                                it.body = it.getBody(cryptoMessage)
                            }
                        }
                    }
                }
                listMessages
            }
            .asLiveData()

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
        if (BuildConfig.ENCRYPT_API) {
            messageList.forEach { message: Message ->
                message.encryptBody(cryptoMessage)
            }
        }
        messageDao.insertMessageList(messageList)
    }

    override fun updateMessage(message: Message) {
        /*if (BuildConfig.ENCRYPT_API) {
            message.encryptBody(cryptoMessage)
        }*/
        messageDao.updateMessage(message)
    }

    override suspend fun updateStateSelectionMessage(
        contactId: Int,
        idMessage: Int,
        isSelected: Int
    ) {
        messageDao.updateMessagesSelected(contactId, idMessage, isSelected)
    }

    override suspend fun cleanSelectionMessages(contactId: Int) {
        messageDao.cleanSelectionMessages(contactId)
    }

    override suspend fun deleteMessagesSelected(
        contactId: Int,
        listMessages: List<MessageAndAttachment>
    ) {
        listMessages.forEach { messageAndAttachment ->
            if (messageAndAttachment.attachmentList.isNotEmpty()) {
                messageAndAttachment.attachmentList.forEach { attachment: Attachment ->
                    attachment.deleteFile(context)
                }
            }
            messageDao.deleteMessagesSelected(contactId, messageAndAttachment.message.id)
        }
    }

    override suspend fun deleteMessagesByStatusForMe(contactId: Int, status: Int) {
        messageDao.getMessagesByStatusForMe(contactId, status)
            .forEach { messageAndAttachment: MessageAndAttachment ->
                if (messageAndAttachment.attachmentList.isNotEmpty()) {
                    messageAndAttachment.attachmentList.forEach { attachment: Attachment ->
                        attachment.deleteFile(context)
                    }
                }
            }
        messageDao.deleteMessagesByStatusForMe(contactId, status)
    }

    override suspend fun getLastMessageByContact(contactId: Int): MessageAndAttachment {
        val messageAndAttachment = messageDao.getLastMessageByContact(contactId)

        with(messageAndAttachment.message) {
            this.let {
                it.body = it.getBody(cryptoMessage)
            }
        }

        return messageAndAttachment
    }

    override suspend fun copyMessagesSelected(contactId: Int): List<String> {
        val messages = messageDao.copyMessagesSelected(contactId)
        val returnMessages = arrayListOf<String>()

        if (BuildConfig.ENCRYPT_API) {
            messages.forEach { returnMessages.add(cryptoMessage.decryptMessageBody(it)) }
        }

        return returnMessages
    }

    override suspend fun getMessagesSelected(contactId: Int): LiveData<List<MessageAndAttachment>> {
        val listMessages = messageDao.getMessagesSelected(contactId)

        if (BuildConfig.ENCRYPT_API) {
            listMessages.value?.forEach { messageAndAttachment: MessageAndAttachment ->
                with(messageAndAttachment.message) {
                    this.let {
                        it.body = it.getBody(cryptoMessage)
                    }
                }
            }
        }
        return listMessages
    }

    override suspend fun updateMessageStatus(messagesWebIds: List<String>, status: Int) {
        messagesWebIds.forEach { messageWebId ->
            val message = getMessageByWebId(messageWebId, false)

            message?.let { messageAndAttachment ->
                if (messageAndAttachment.message.status != Constants.MessageStatus.READED.status) {
                    when (status) {
                        Constants.MessageStatus.READED.status -> {
                            val timeByMessage =
                                messageDao.getSelfDestructTimeByMessage(messageWebId)
                            val currentTime =
                                TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
                            val time =
                                currentTime.plus(Utils.convertItemOfTimeInSeconds(timeByMessage))

                            messageDao.updateMessageStatus(messageWebId, currentTime, time, status)
                        }
                        else -> {
                            messageDao.updateMessageStatus(messageWebId, 0, 0, status)
                        }
                    }
                }
            }
        }
    }

    override fun getMessagesForHome(): LiveData<List<MessageAndAttachment>> {
        return messageDao.getMessagesForHome()
            .map { listMessages: List<MessageAndAttachment> ->
                if (BuildConfig.ENCRYPT_API) {
                    listMessages.forEach { messageAndAttachment: MessageAndAttachment ->
                        with(messageAndAttachment.message) {
                            this.let {
                                it.body = it.getBody(cryptoMessage)
                            }
                        }
                    }
                }
                listMessages
            }
            .asLiveData()
    }

    override suspend fun getTextMessagesByStatus(
        contactId: Int,
        status: Int
    ): List<MessageAndAttachment> {
        return messageDao.getTextMessagesByStatus(contactId, status)
    }

    override suspend fun deleteMessages(contactId: Int) {
        messageDao.getMessagesByContact(contactId)
            .forEach { messageAndAttachment: MessageAndAttachment ->
                if (messageAndAttachment.attachmentList.isNotEmpty()) {
                    messageAndAttachment.attachmentList.forEach { attachment: Attachment ->
                        attachment.deleteFile(context)
                    }
                }
            }
        messageDao.deleteMessages(contactId)
    }

    override suspend fun setSelfDestructTimeByMessages(selfDestructTime: Int, contactId: Int) {
        messageDao.setSelfDestructTimeByMessages(selfDestructTime, contactId)
    }

    override suspend fun deletedMessages(listWebIdMessages: List<String>) {
        listWebIdMessages.forEach { webId ->
            messageDao.getMessageByWebId(webId)?.let { messageAndAttachment ->
                if (messageAndAttachment.attachmentList.isNotEmpty()) {
                    messageAndAttachment.attachmentList.forEach { attachment: Attachment ->
                        attachment.deleteFile(context)
                    }
                }
            }
            messageDao.deletedMessages(webId)
        }
    }

    override suspend fun getIdContactWithWebId(listWebId: List<String>): Int {
        return messageDao.getIdContactWithWebId(listWebId[0])
    }

    override fun verifyMessagesToDelete() {
        messageDao.verifyMessagesToDelete()
    }
}