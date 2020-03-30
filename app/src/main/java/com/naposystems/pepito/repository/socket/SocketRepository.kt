package com.naposystems.pepito.repository.socket

import android.content.Context
import com.naposystems.pepito.db.dao.attachment.AttachmentDataSource
import com.naposystems.pepito.db.dao.conversation.ConversationDataSource
import com.naposystems.pepito.db.dao.message.MessageDataSource
import com.naposystems.pepito.db.dao.quoteMessage.QuoteDataSource
import com.naposystems.pepito.dto.conversation.attachment.AttachmentResDTO
import com.naposystems.pepito.dto.conversation.message.MessageResDTO
import com.naposystems.pepito.entity.message.Quote
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.FileManager
import com.naposystems.pepito.webService.NapoleonApi
import com.naposystems.pepito.webService.socket.IContractSocketService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SocketRepository @Inject constructor(
    private val context: Context,
    private val napoleonApi: NapoleonApi,
    private val conversationLocalDataSource: ConversationDataSource,
    private val messageLocalDataSource: MessageDataSource,
    private val attachmentLocalDataSource: AttachmentDataSource,
    private val quoteDataSource: QuoteDataSource
) : IContractSocketService.Repository {

    override fun getMyMessages() {
        GlobalScope.launch {
            val response = napoleonApi.getMyMessages()

            if (response.isSuccessful) {
                val messageResList: MutableList<MessageResDTO> = response.body()!!.toMutableList()

                if (messageResList.isNotEmpty()) {

                    for (messageRes in messageResList) {

                        val message = MessageResDTO.toMessageEntity(
                            null, messageRes, Constants.IsMine.NO.value
                        )

                        val messageId = messageLocalDataSource.insertMessage(message)

                        if (messageRes.quoted.isNotEmpty()) {
                            insertQuote(messageRes, messageId.toInt())
                        }

                        val listAttachments = AttachmentResDTO.toListConversationAttachment(
                            messageId.toInt(),
                            messageRes.attachments
                        )

                        downloadFile(listAttachments)

                        attachmentLocalDataSource.insertAttachments(listAttachments)

                        val unreadMessages =
                            messageResList.filter { it.userDestination == messageRes.userDestination }
                                .size

                        conversationLocalDataSource.insertConversation(
                            messageRes,
                            false,
                            unreadMessages
                        )
                    }

                }
            }
        }
    }

    private suspend fun downloadFile(listAttachments: List<Attachment>) {
        withContext(Dispatchers.IO) {
            listAttachments.forEach { attachment ->

                val responseDownloadFile =
                    napoleonApi.downloadFileByUrl(attachment.body)

                if (responseDownloadFile.isSuccessful) {
                    attachment.uri =
                        FileManager.saveToDisk(
                            context = context,
                            body = responseDownloadFile.body()!!,
                            type = attachment.type,
                            extension = attachment.extension
                        )
                }
            }
        }
    }

    private fun insertQuote(messageRes: MessageResDTO, messageId: Int) {
        val originalMessage =
            messageLocalDataSource.getMessageByWebId(messageRes.quoted)

        var firstAttachment: Attachment? = null

        if (originalMessage.attachmentList.isNotEmpty()) {
            firstAttachment = originalMessage.attachmentList.first()
        }

        val quote = Quote(
            id = 0,
            messageId = messageId,
            contactId = originalMessage.message.contactId,
            body = originalMessage.message.body,
            attachmentType = firstAttachment?.type ?: "",
            thumbnailUri = firstAttachment?.uri ?: "",
            messageParentId = originalMessage.message.id,
            isMine = originalMessage.message.isMine
        )

        quoteDataSource.insertQuote(quote)
    }

    override fun verifyMessagesReceived() {
        GlobalScope.launch {
            val response = napoleonApi.verifyMessagesReceived()

            if (response.isSuccessful) {
                messageLocalDataSource.updateMessageStatus(
                    response.body()!!,
                    Constants.MessageStatus.UNREAD.status
                )
            }
        }
    }

    override fun verifyMessagesRead() {
        GlobalScope.launch {
            val response = napoleonApi.verifyMessagesRead()

            if (response.isSuccessful) {
                messageLocalDataSource.updateMessageStatus(
                    response.body()!!,
                    Constants.MessageStatus.READED.status
                )
            }
        }
    }

    override fun getDeletedMessages() {
        GlobalScope.launch {
            val response = napoleonApi.getDeletedMessages()
            if (response.isSuccessful && (response.body()!!.count() > 0)) {
                val idContact = messageLocalDataSource.getIdContactWithWebId(response.body()!!)
                messageLocalDataSource.deletedMessages(response.body()!!)
                when (val messageAndAttachment =
                    messageLocalDataSource.getLastMessageByContact(idContact)) {
                    null -> {
                        conversationLocalDataSource.cleanConversation(idContact)
                    }
                    else -> {
                        conversationLocalDataSource.getQuantityUnreads(idContact)
                            .let { quantityUnreads ->
                                if (quantityUnreads > 0) {
                                    conversationLocalDataSource.updateConversationByContact(
                                        idContact,
                                        messageAndAttachment.message.body,
                                        messageAndAttachment.message.createdAt,
                                        messageAndAttachment.message.status,
                                        quantityUnreads - response.body()!!.count()
                                    )
                                } else {
                                    conversationLocalDataSource.updateConversationByContact(
                                        idContact,
                                        messageAndAttachment.message.body,
                                        messageAndAttachment.message.createdAt,
                                        messageAndAttachment.message.status,
                                        0
                                    )
                                }
                            }
                    }
                }
            }
        }
    }
}