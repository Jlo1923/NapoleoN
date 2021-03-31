package com.naposystems.napoleonchat.source.local.datasource.message

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.source.local.dao.ContactDao
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.dao.MessageDao
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MessageLocalDataSourceImp @Inject constructor(
    private val cryptoMessage: CryptoMessage,
    private val context: Context,
    private val contactDao: ContactDao,
    private val messageDao: MessageDao
) : MessageLocalDataSource {

    override suspend fun getMessageByWebId(webId: String, decrypt: Boolean): MessageAttachmentRelation? {
        val messageAndAttachment = messageDao.getMessageByWebId(webId)
        if (BuildConfig.ENCRYPT_API && decrypt) {
            with(messageAndAttachment?.messageEntity) {
                this?.let {
                    it.body = it.getBody(cryptoMessage)
                }
            }
        }

        return messageAndAttachment
    }

    override suspend fun getMessageById(id: Int, decrypt: Boolean): MessageAttachmentRelation? {
        val messageAndAttachmentRelation = messageDao.getMessageById(id)
        if (BuildConfig.ENCRYPT_API && decrypt) {
            with(messageAndAttachmentRelation?.messageEntity) {
                this?.let {
                    it.body = it.getBody(cryptoMessage)
                }
            }
        }

        return messageAndAttachmentRelation
    }

    override fun getMessages(contactId: Int) =
        messageDao.getMessagesAndAttachmentsDistinctUntilChanged(contactId)
            .map { listMessageRelations: List<MessageAttachmentRelation> ->

                val mutableListMessageRelations: MutableList<MessageAttachmentRelation> = arrayListOf()

                if (BuildConfig.ENCRYPT_API) {
                    listMessageRelations.forEach { messageAndAttachmentRelation: MessageAttachmentRelation ->
                        with(messageAndAttachmentRelation.messageEntity) {
                            this.let {
                                it.body = it.getBody(cryptoMessage)
                            }
                        }
                    }
                }

                var dayOfYear = -1

                listMessageRelations.forEachIndexed { _, messageAndAttachment ->
                    val timeStamp =
                        TimeUnit.SECONDS.toMillis(messageAndAttachment.messageEntity.createdAt.toLong())

                    val messageDate =
                        Date(timeStamp)

                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val dayMessage = sdf.format(messageDate)

                    if (dayOfYear != getDayOfYear(messageDate)) {
                        dayOfYear = getDayOfYear(messageDate)

                        val message = MessageAttachmentRelation(
                            MessageEntity(
                                id = -1,
                                webId = "",
                                uuid = null,
                                body = dayMessage,
                                quoted = "",
                                contactId = messageAndAttachment.messageEntity.contactId,
                                updatedAt = messageAndAttachment.messageEntity.updatedAt,
                                createdAt = messageAndAttachment.messageEntity.createdAt,
                                isMine = Constants.IsMine.YES.value,
                                status = Constants.MessageStatus.SENT.status,
                                numberAttachments = 0,
                                selfDestructionAt = messageAndAttachment.messageEntity.selfDestructionAt,
                                totalSelfDestructionAt = messageAndAttachment.messageEntity.totalSelfDestructionAt,
                                messageType = Constants.MessageType.MESSAGES_GROUP_DATE.type
                            ),
                            attachmentEntityList = arrayListOf(),
                            quoteEntity = null,
                            contact = messageAndAttachment.contact
                        )

                        mutableListMessageRelations.add(message)
                    }

                    mutableListMessageRelations.add(messageAndAttachment)
                }

                mutableListMessageRelations.filter {
                    it.messageEntity.numberAttachments == 0 || (it.messageEntity.numberAttachments > 0 && it.attachmentEntityList.count() > 0)
                }.filter {
                    (it.messageEntity.quoted.isEmpty() && it.quoteEntity == null) || (it.messageEntity.quoted.isNotEmpty() && it.quoteEntity != null)
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

    override fun getLocalMessagesByStatus(contactId: Int, status: Int): List<MessageAttachmentRelation> {
        return messageDao.getLocalMessagesByStatus(contactId, status)
    }

    override suspend fun insertMessage(messageEntity: MessageEntity): Long {
        Timber.d("insertMessage: DATASOURCE $messageEntity")
        return messageDao.insertMessage(messageEntity)
    }

    override fun insertListMessage(messageEntityList: List<MessageEntity>) {
        if (BuildConfig.ENCRYPT_API) {
            messageEntityList.forEach { messageEntity: MessageEntity ->
                messageEntity.encryptBody(cryptoMessage)
            }
        }
        messageDao.insertMessageList(messageEntityList)
    }

    override fun updateMessage(messageEntity: MessageEntity) {
        /*if (BuildConfig.ENCRYPT_API) {
            message.encryptBody(cryptoMessage)
        }*/
        messageDao.updateMessage(messageEntity)
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
        listMessageRelations: List<MessageAttachmentRelation>
    ) {
        listMessageRelations.forEach { messageAndAttachment ->
            if (messageAndAttachment.attachmentEntityList.isNotEmpty()) {
                messageAndAttachment.attachmentEntityList.forEach { attachmentEntity: AttachmentEntity ->
                    attachmentEntity.deleteFile(context)
                }
            }
            messageDao.deleteMessagesSelected(contactId, messageAndAttachment.messageEntity.id)
        }
    }

    override suspend fun deleteMessagesByStatusForMe(contactId: Int, status: Int) {
        messageDao.getMessagesByStatusForMe(contactId, status)
            .forEach { messageAndAttachmentRelation: MessageAttachmentRelation ->
                if (messageAndAttachmentRelation.attachmentEntityList.isNotEmpty()) {
                    messageAndAttachmentRelation.attachmentEntityList.forEach { attachmentEntity: AttachmentEntity ->
                        attachmentEntity.deleteFile(context)
                    }
                }
            }
        messageDao.deleteMessagesByStatusForMe(contactId, status)
    }

    override suspend fun getLastMessageByContact(contactId: Int): MessageAttachmentRelation {
        val messageAndAttachment = messageDao.getLastMessageByContact(contactId)

        with(messageAndAttachment.messageEntity) {
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

    override suspend fun getMessagesSelected(contactId: Int): LiveData<List<MessageAttachmentRelation>> {
        val listMessages = messageDao.getMessagesSelected(contactId)

        if (BuildConfig.ENCRYPT_API) {
            listMessages.value?.forEach { messageAndAttachmentRelation: MessageAttachmentRelation ->
                with(messageAndAttachmentRelation.messageEntity) {
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
                if (messageAndAttachment.messageEntity.status != Constants.MessageStatus.READED.status) {
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

    override fun getMessagesForHome(): LiveData<List<MessageAttachmentRelation>> {
        return messageDao.getMessagesForHome()
            .map { listMessageRelations: List<MessageAttachmentRelation> ->
                if (BuildConfig.ENCRYPT_API) {
                    listMessageRelations.forEach { messageAndAttachmentRelation: MessageAttachmentRelation ->
                        with(messageAndAttachmentRelation.messageEntity) {
                            this.let {
                                it.body = it.getBody(cryptoMessage)
                            }
                        }
                    }
                }
                listMessageRelations
            }
            .asLiveData()
    }

    override suspend fun getTextMessagesByStatus(
        contactId: Int,
        status: Int
    ): List<MessageAttachmentRelation> {
        return messageDao.getTextMessagesByStatus(contactId, status)
    }

    override suspend fun getMissedCallsByStatus(
        contactId: Int,
        status: Int
    ): List<MessageAttachmentRelation> {
        return messageDao.getMissedCallsByStatus(contactId, status)
    }

    override suspend fun deleteMessages(contactId: Int) {
        messageDao.getMessagesByContact(contactId)
            .forEach { messageAndAttachmentRelation: MessageAttachmentRelation ->
                if (messageAndAttachmentRelation.attachmentEntityList.isNotEmpty()) {
                    messageAndAttachmentRelation.attachmentEntityList.forEach { attachmentEntity: AttachmentEntity ->
                        attachmentEntity.deleteFile(context)
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
                if (messageAndAttachment.attachmentEntityList.isNotEmpty()) {
                    messageAndAttachment.attachmentEntityList.forEach { attachmentEntity: AttachmentEntity ->
                        attachmentEntity.deleteFile(context)
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