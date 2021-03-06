package com.naposystems.napoleonchat.source.local.datasource.message

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.source.local.dao.AttachmentDao
import com.naposystems.napoleonchat.source.local.dao.ContactDao
import com.naposystems.napoleonchat.source.local.dao.MessageDao
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MessageLocalDataSourceImp @Inject constructor(
    private val cryptoMessage: CryptoMessage,
    private val context: Context,
    private val contactDao: ContactDao,
    private val messageDao: MessageDao,
    private val attachmentDao: AttachmentDao
) : MessageLocalDataSource {

    override suspend fun getMessageByWebId(
        webId: String,
        decrypt: Boolean
    ): MessageAttachmentRelation? {
        val messageAndAttachment = messageDao.getMessageByWebId(webId)
        if (BuildConfig.ENCRYPT_API && decrypt) {
            with(messageAndAttachment?.messageEntity) {
                this?.let { it.body = it.getBody(cryptoMessage) }
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

    override suspend fun getMessageByIdAsLiveData(
        id: Int,
        decrypt: Boolean
    ): LiveData<List<AttachmentEntity>> {
        return messageDao.getMessageByIdAsFlow(id).map { attachments ->
            attachments
        }.asLiveData()
    }

    override fun getMessages(contactId: Int) =
        messageDao.getMessagesAndAttachmentsDistinctUntilChanged(contactId)
            .map { listMessageRelations: List<MessageAttachmentRelation> ->

                val mutableListMessageRelations: MutableList<MessageAttachmentRelation> =
                    arrayListOf()

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
                                uuid = UUID.randomUUID().toString(),
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
                                messageType = Constants.MessageTextType.GROUP_DATE.type
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
        return messageDao.getMessageIdByQuoteWebId(quoteWebId)
    }

    override fun getLocalMessagesByStatus(
        contactId: Int,
        status: Int
    ): List<MessageAttachmentRelation> {
        return messageDao.getMessagesByStatus(contactId, status)
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
        val messageAndAttachment = messageDao.getLastMessageByContactId(contactId)

        with(messageAndAttachment.messageEntity) {
            this.let {
                it.body = it.getBody(cryptoMessage)
            }
        }

        return messageAndAttachment
    }

    override suspend fun copyMessagesSelected(contactId: Int): List<String> {
        val messages = messageDao.copyMessagesSelected(contactId)

        return messages
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

    override suspend fun updateMessageStatusBeforeAttachment(attachmentsWebIds: List<String>) {

        val listAttachments = mutableListOf<AttachmentEntity>()

        attachmentsWebIds.forEach {
            attachmentDao.getAttachmentByWebId(it)?.let {
                listAttachments.add(it)
            }
        }

        val listMessages: List<String> = listAttachments.map {
            it.messageWebId
        }.distinct().map {
            messageDao.getMessageByWebId(it)
        }.filter {
            it?.attachmentEntityList?.size == it?.messageEntity?.numberAttachments
        }.map {
            it?.messageEntity?.webId.toString()
        }

        listMessages.let {
            updateMessageStatus(it, Constants.MessageStatus.UNREAD.status)
        }
    }

    override suspend fun updateMessageStatus(
        messagesWebIds: List<String>,
        status: Int
    ) {
        Timber.d("updateMessageStatus status: $status")
        messagesWebIds.forEach { messageWebId ->
            val message = getMessageByWebId(messageWebId, false)

            message?.let { messageAndAttachment ->
                if (messageAndAttachment.messageEntity.status != Constants.MessageStatus.READED.status) {
                    val timeByMessage = messageDao.getMessageSelfDestructTimeById(messageWebId)
                    val currentTime =
                        TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
                    when (status) {
                        Constants.MessageStatus.READED.status -> {
                            val time =
                                currentTime.plus(Utils.convertItemOfTimeInSeconds(timeByMessage))
                            messageDao.updateMessageStatus(
                                messageWebId,
                                currentTime,
                                time,
                                status
                            )
                        }
                        else -> {
                            when (timeByMessage) {
                                Constants.SelfDestructTime.EVERY_TWENTY_FOUR_HOURS_ERROR.time,
                                Constants.SelfDestructTime.EVERY_SEVEN_DAYS_ERROR.time -> {
                                    val contactId = messageDao.getContactIdByWebId(messageWebId)
                                    val messageAndAttachment =
                                        messageDao.getMessageByWebId(messageWebId)
                                    val timeContact =
                                        contactDao.getSelfDestructTimeByContactWithOutLiveData(
                                            contactId
                                        )
                                    val durationAttachment = TimeUnit.MILLISECONDS.toSeconds(
                                        messageAndAttachment?.getFirstAttachment()?.duration
                                            ?: 0
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

    override fun getMessagesForHome(): LiveData<MutableList<MessageAttachmentRelation>> {

        val messages = messageDao.getMessagesForHome()
            .map { listMessageRelations: MutableList<MessageAttachmentRelation> ->
                if (BuildConfig.ENCRYPT_API) {
                    listMessageRelations.forEach { messageAndAttachmentRelation: MessageAttachmentRelation ->
                        val unReadMsgs = messageAndAttachmentRelation.contact?.id?.let {
                            messageDao.countUnreadByContactId(it)
                        } ?: run { 0 }

                        val unreadAttachments = messageAndAttachmentRelation.contact?.id?.let {
                            val msgsByContact = messageDao.getMessagesByContactNotMine(it)
                            var countAttachmentsUnread = 0
                            msgsByContact.forEach { msgAndRelation ->
                                if (msgAndRelation.messageEntity.numberAttachments > 1) {
                                    val numberAttachments =
                                        msgAndRelation.messageEntity.numberAttachments
                                    val attachmentsRead =
                                        msgAndRelation.attachmentEntityList.count {
                                            it.status == Constants.AttachmentStatus.READED.status
                                        }
                                    countAttachmentsUnread += (numberAttachments - attachmentsRead)
                                }
                            }
                            countAttachmentsUnread
                        } ?: run { 0 }

                        messageAndAttachmentRelation.messagesUnReads =
                            unReadMsgs + unreadAttachments

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

        return messages
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

    override suspend fun deleteMessagesByContactId(contactId: Int) {
        messageDao.getMessagesByContact(contactId)
            .forEach { messageAndAttachmentRelation: MessageAttachmentRelation ->
                if (messageAndAttachmentRelation.attachmentEntityList.isNotEmpty()) {
                    messageAndAttachmentRelation.attachmentEntityList.forEach { attachmentEntity: AttachmentEntity ->
                        attachmentEntity.deleteFile(context)
                    }
                }
            }
        messageDao.deleteMessagesByContactId(contactId)
    }

    override suspend fun setSelfDestructTimeByMessages(selfDestructTime: Int, contactId: Int) {
        messageDao.setSelfDestructTimeByMessages(selfDestructTime, contactId)
    }

    override suspend fun deleteMessagesByWebId(listWebIdMessages: List<String>) {
        listWebIdMessages.forEach { webId ->
            messageDao.getMessageByWebId(webId)?.let { messageAndAttachment ->
                if (messageAndAttachment.attachmentEntityList.isNotEmpty()) {
                    messageAndAttachment.attachmentEntityList.forEach { attachmentEntity: AttachmentEntity ->
                        attachmentEntity.deleteFile(context)
                        attachmentDao.deletedAttachment(attachmentEntity.webId)
                    }
                }
            }
            messageDao.deleteMessagesByWebId(webId)
        }
    }

    override suspend fun getContactIdByWebId(listWebId: List<String>): Int {
        return messageDao.getContactIdByWebId(listWebId[0])
    }

    override fun deleteMessagesByTotalSelfDestructionAt() {
        messageDao.deleteMessagesByTotalSelfDestructionAt()
    }

    override suspend fun deleteMessageByContactIdAndType(contactId: Int, type: Int) {
        return messageDao.deleteMessageByContactIdAndType(contactId, type)
    }

    //Funci??n para limpiar la conversaci??n de mensajes exitosos duplicados
    override suspend fun deleteDuplicateMessage() {
        return messageDao.deleteDuplicateMessage()
    }

    override suspend fun addUUIDMessage() {
        return messageDao.addUUIDMessage()
    }

    //Funci??n para limpiar la conversaci??n de attachment exitosos duplicados
    override suspend fun deleteDuplicateAttachment() {
        return messageDao.deleteDuplicateAttachment()
    }

    override suspend fun addUUIDAttachment() {
        return messageDao.addUUIDAttachment()
    }
}