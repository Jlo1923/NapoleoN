package com.naposystems.napoleonchat.repository.socket

import android.content.Context
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.db.dao.attachment.AttachmentDataSource
import com.naposystems.napoleonchat.db.dao.contact.ContactDataSource
import com.naposystems.napoleonchat.db.dao.message.MessageDataSource
import com.naposystems.napoleonchat.db.dao.quoteMessage.QuoteDataSource
import com.naposystems.napoleonchat.dto.contacts.ContactResDTO
import com.naposystems.napoleonchat.dto.conversation.attachment.AttachmentResDTO
import com.naposystems.napoleonchat.dto.conversation.call.reject.RejectCallReqDTO
import com.naposystems.napoleonchat.dto.conversation.message.MessageResDTO
import com.naposystems.napoleonchat.entity.message.Quote
import com.naposystems.napoleonchat.entity.message.attachments.Attachment
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.webService.NapoleonApi
import com.naposystems.napoleonchat.webService.socket.IContractSocketService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SocketRepository @Inject constructor(
    private val context: Context,
    private val napoleonApi: NapoleonApi,
    private val messageLocalDataSource: MessageDataSource,
    private val attachmentLocalDataSource: AttachmentDataSource,
    private val quoteDataSource: QuoteDataSource,
    private val contactLocalDataSource: ContactDataSource
) : IContractSocketService.Repository {

    val cryptoMessage = CryptoMessage(context)

    override suspend fun getContacts() {
        try {
            val response = napoleonApi.getContactsByState(Constants.FriendShipState.ACTIVE.state)

            if (response.isSuccessful) {

                val contactResDTO = response.body()!!

                val contacts = ContactResDTO.toEntityList(contactResDTO.contacts)

                val contactsToDelete = contactLocalDataSource.insertOrUpdateContactList(contacts)

                if (contactsToDelete.isNotEmpty()) {

                    contactsToDelete.forEach { contact ->
                        messageLocalDataSource.deleteMessageByType(
                            contact.id,
                            Constants.MessageType.NEW_CONTACT.type
                        )
                        contactLocalDataSource.deleteContact(contact)
                    }
                }
            } else {
                Timber.e(response.errorBody()!!.string())
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun getMyMessages(contactId: Int?) {
        GlobalScope.launch {
            try {
                getContacts()
                val response = napoleonApi.getMyMessages()

                if (response.isSuccessful) {
                    val messageResList: MutableList<MessageResDTO> =
                        response.body()!!.toMutableList()

                    if (messageResList.isNotEmpty()) {

                        for (messageRes in messageResList) {

                            val databaseMessage =
                                messageLocalDataSource.getMessageByWebId(messageRes.id, false)

                            if (databaseMessage == null) {
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

                                contactId?.let {
                                    RxBus.publish(
                                        RxEvent.NewMessageEventForCounter(contactId)
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    override fun deleteContact(contactId: Int?) {
        GlobalScope.launch {
            contactId?.let {
                contactLocalDataSource.getContactById(contactId)?.let { contact ->
                    contactLocalDataSource.deleteContact(contact)
                }
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
                thumbnailUri = firstAttachment?.fileName ?: "",
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