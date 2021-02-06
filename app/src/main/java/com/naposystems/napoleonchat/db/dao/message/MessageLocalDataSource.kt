package com.naposystems.napoleonchat.db.dao.message

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.db.dao.contact.ContactDao
import com.naposystems.napoleonchat.entity.message.Message
import com.naposystems.napoleonchat.entity.message.MessageAndAttachment
import com.naposystems.napoleonchat.entity.message.attachments.Attachment
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.mediaPlayer.MediaPlayerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MessageLocalDataSource @Inject constructor(
    private val context: Context,
    private val contactDao: ContactDao,
    private val messageDao: MessageDao
) : MessageDataSource {

    private val cryptoMessage = CryptoMessage(context)

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

                val mutableListMessages: MutableList<MessageAndAttachment> = arrayListOf()

                if (BuildConfig.ENCRYPT_API) {
                    listMessages.forEach { messageAndAttachment: MessageAndAttachment ->
                        with(messageAndAttachment.message) {
                            this.let {
                                it.body = it.getBody(cryptoMessage)
                            }
                        }
                    }
                }

                var dayOfYear = -1

                listMessages.forEachIndexed { _, messageAndAttachment ->
                    val timeStamp =
                        TimeUnit.SECONDS.toMillis(messageAndAttachment.message.createdAt.toLong())

                    val messageDate =
                        Date(timeStamp)

                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val dayMessage = sdf.format(messageDate)

                    if (dayOfYear != getDayOfYear(messageDate)) {
                        dayOfYear = getDayOfYear(messageDate)

                        val message = MessageAndAttachment(
                            Message(
                                id = -1,
                                webId = "",
                                uuid = null,
                                body = dayMessage,
                                quoted = "",
                                contactId = messageAndAttachment.message.contactId,
                                updatedAt = messageAndAttachment.message.updatedAt,
                                createdAt = messageAndAttachment.message.createdAt,
                                isMine = Constants.IsMine.YES.value,
                                status = Constants.MessageStatus.SENT.status,
                                numberAttachments = 0,
                                selfDestructionAt = messageAndAttachment.message.selfDestructionAt,
                                totalSelfDestructionAt = messageAndAttachment.message.totalSelfDestructionAt,
                                messageType = Constants.MessageType.MESSAGES_GROUP_DATE.type
                            ),
                            attachmentList = arrayListOf(),
                            quote = null,
                            contact = messageAndAttachment.contact
                        )

                        mutableListMessages.add(message)
                    }

                    mutableListMessages.add(messageAndAttachment)
                }

                mutableListMessages.filter {
                    it.message.numberAttachments == 0 || (it.message.numberAttachments > 0 && it.attachmentList.count() > 0)
                }.filter {
                    (it.message.quoted.isEmpty() && it.quote == null) || (it.message.quoted.isNotEmpty() && it.quote != null)
                }.toList()

            }
            .asLiveData()

    private fun getDayOfYear(date: Date): Int {
        val calendar = dateToCalendar(date)
        return calendar.get(Calendar.DAY_OF_YEAR)
    }

    private fun dateToCalendar(date: Date): Calendar {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.timeInMillis = date.time
        return calendar
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

    override fun existMessage(id: String): Boolean {
        return messageDao.existMessage(id) != null
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
                    val timeByMessage = messageDao.getSelfDestructTimeByMessage(messageWebId)
                    val currentTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
                    when (status) {
                        Constants.MessageStatus.READED.status -> {
                            val time =
                                currentTime.plus(Utils.convertItemOfTimeInSeconds(timeByMessage))
                            messageDao.updateMessageStatus(messageWebId, currentTime, time, status)
                        }
                        else -> {
                            when (timeByMessage) {
                                Constants.SelfDestructTime.EVERY_TWENTY_FOUR_HOURS_ERROR.time,
                                Constants.SelfDestructTime.EVERY_SEVEN_DAYS_ERROR.time -> {
                                    val contactId = messageDao.getContactByMessage(messageWebId)
                                    val messageAndAttachment =
                                        messageDao.getMessageByWebId(messageWebId)
                                    val timeContact =
                                        contactDao.getSelfDestructTimeByContactWithOutLiveData(
                                            contactId
                                        )
                                    val durationAttachment = TimeUnit.MILLISECONDS.toSeconds(
                                        messageAndAttachment?.getFirstAttachment()?.duration ?: 0
                                    ).toInt()
                                    val selfAutoDestruction =
                                        Utils.compareDurationAttachmentWithSelfAutoDestructionInSeconds(
                                            durationAttachment, timeContact
                                        )

                                    messageDao.updateSelfDestructTimeByMessages(
                                        selfAutoDestruction,
                                        messageWebId,
                                        status
                                    )
                                }
                                else -> {
                                    messageDao.updateMessageStatus(messageWebId, 0, 0, status)
                                }
                            }
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

    override suspend fun getMissedCallsByStatus(
        contactId: Int,
        status: Int
    ): List<MessageAndAttachment> {
        return messageDao.getMissedCallsByStatus(contactId, status)
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
                        withContext(Dispatchers.Main) {
                            MediaPlayerManager.resetMediaPlayer(messageAndAttachment.message.id.toString())
                        }
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

    override suspend fun deleteMessageByType(contactId: Int, type: Int) {
        return messageDao.deleteMessageByType(contactId, type)
    }
}