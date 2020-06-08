package com.naposystems.pepito.repository.socket

import android.content.Context
import com.naposystems.pepito.BuildConfig
import com.naposystems.pepito.crypto.message.CryptoMessage
import com.naposystems.pepito.db.dao.attachment.AttachmentDataSource
import com.naposystems.pepito.db.dao.message.MessageDataSource
import com.naposystems.pepito.db.dao.quoteMessage.QuoteDataSource
import com.naposystems.pepito.dto.conversation.attachment.AttachmentResDTO
import com.naposystems.pepito.dto.conversation.call.reject.RejectCallReqDTO
import com.naposystems.pepito.dto.conversation.message.MessageResDTO
import com.naposystems.pepito.entity.message.Quote
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.webService.NapoleonApi
import com.naposystems.pepito.webService.socket.IContractSocketService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SocketRepository @Inject constructor(
    private val context: Context,
    private val napoleonApi: NapoleonApi,
    private val messageLocalDataSource: MessageDataSource,
    private val attachmentLocalDataSource: AttachmentDataSource,
    private val quoteDataSource: QuoteDataSource
) : IContractSocketService.Repository {

    val cryptoMessage = CryptoMessage(context)

    override fun getMyMessages() {
        GlobalScope.launch {
            try {
                val response = napoleonApi.getMyMessages()

                if (response.isSuccessful) {
                    val messageResList: MutableList<MessageResDTO> =
                        response.body()!!.toMutableList()

                    if (messageResList.isNotEmpty()) {

                        for (messageRes in messageResList) {

                            val message = MessageResDTO.toMessageEntity(
                                null, messageRes, Constants.IsMine.NO.value
                            )

                            if (BuildConfig.ENCRYPT_API) {
                                message.encryptBody(cryptoMessage)
                            }

                            val messageId = messageLocalDataSource.insertMessage(message)
                            Timber.d("Conversation insertó mensajes")

                            if (messageRes.quoted.isNotEmpty()) {
                                insertQuote(messageRes, messageId.toInt())
                            }

                            val listAttachments = AttachmentResDTO.toListConversationAttachment(
                                messageId.toInt(),
                                messageRes.attachments
                            )

                            attachmentLocalDataSource.insertAttachments(listAttachments)
                            Timber.d("Conversation insertó attachment")
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private suspend fun insertQuote(messageRes: MessageResDTO, messageId: Int) {
        val originalMessage =
            messageLocalDataSource.getMessageByWebId(messageRes.quoted, false)

        if (originalMessage != null) {
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
                messageLocalDataSource.deletedMessages(response.body()!!)
            }
        }
    }

    override fun rejectCall(contactId: Int, channel: String) {
        GlobalScope.launch {
            val rejectCallReqDTO = RejectCallReqDTO(
                contactId = contactId,
                channel = channel
            )
            val response = napoleonApi.rejectCall(rejectCallReqDTO)

            if (response.isSuccessful) {
                Timber.d("LLamada rechazada bb")
            }
        }
    }
}