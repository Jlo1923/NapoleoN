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
import com.naposystems.napoleonchat.dto.conversation.call.readyForCall.ReadyForCallReqDTO
import com.naposystems.napoleonchat.dto.conversation.call.reject.RejectCallReqDTO
import com.naposystems.napoleonchat.dto.conversation.message.MessageReceivedReqDTO
import com.naposystems.napoleonchat.dto.conversation.message.MessageResDTO
import com.naposystems.napoleonchat.dto.conversation.message.MessagesReadReqDTO
import com.naposystems.napoleonchat.dto.newMessageEvent.NewMessageDataEventRes
import com.naposystems.napoleonchat.dto.newMessageEvent.NewMessageEventAttachmentRes
import com.naposystems.napoleonchat.entity.message.Quote
import com.naposystems.napoleonchat.entity.message.attachments.Attachment
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Data
import com.naposystems.napoleonchat.webService.NapoleonApi
import com.naposystems.napoleonchat.webService.socket.IContractSocketService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                                    insertQuote(messageRes.quoted, messageId.toInt())
                                }

                                val listAttachments = AttachmentResDTO.toListConversationAttachment(
                                    messageId.toInt(),
                                    messageRes.attachments
                                )

                                attachmentLocalDataSource.insertAttachments(listAttachments)
                                Timber.d("Conversation insertó attachment")

                                if (Data.contactId != 0) {
                                    notifyMessagesReaded()
                                }

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

    override fun insertNewMessage(newMessageDataEventRes: NewMessageDataEventRes) {
        GlobalScope.launch {
            val databaseMessage =
                messageLocalDataSource.getMessageByWebId(
                    newMessageDataEventRes.messageId,
                    false
                )

            if (databaseMessage == null) {

                val message =
                    newMessageDataEventRes.message.toMessageEntity(
                        Constants.IsMine.NO.value
                    )

                if (BuildConfig.ENCRYPT_API) {
                    message.encryptBody(cryptoMessage)
                }

                val messageId =
                    messageLocalDataSource.insertMessage(message)
                Timber.d("Conversation insertó mensajes")

                notifyMessageReceived(newMessageDataEventRes.messageId)

                if (newMessageDataEventRes.message.quoted.isNotEmpty()) {
                    insertQuote(newMessageDataEventRes.message.quoted, messageId.toInt())
                }

                val listAttachments =
                    NewMessageEventAttachmentRes.toListConversationAttachment(
                        messageId.toInt(),
                        newMessageDataEventRes.message.attachments
                    )

                attachmentLocalDataSource.insertAttachments(listAttachments)
                Timber.d("Conversation insertó attachment")

                RxBus.publish(
                    RxEvent.NewMessageEventForCounter(newMessageDataEventRes.contactId)
                )
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

    private suspend fun insertQuote(quoteWebId: String, messageId: Int) {
        val originalMessage =
            messageLocalDataSource.getMessageByWebId(quoteWebId, false)

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

    private fun notifyMessageReceived(messageId: String) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val messageReceivedReqDTO = MessageReceivedReqDTO(messageId)
                    napoleonApi.notifyMessageReceived(messageReceivedReqDTO)
                    Timber.d("notifyMessageReceived")
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
    }

    private fun notifyMessagesReaded() {
        GlobalScope.launch(Dispatchers.IO) {
            val messagesUnread =
                messageLocalDataSource.getTextMessagesByStatus(
                    Data.contactId,
                    Constants.MessageStatus.UNREAD.status
                )

            val textMessagesUnread = messagesUnread.filter {
                it.attachmentList.isEmpty() ||
                        it.message.messageType == Constants.MessageType.MISSED_CALL.type ||
                        it.message.messageType == Constants.MessageType.MISSED_VIDEO_CALL.type
            }

            val locationMessagesUnread = messagesUnread.filter {
                it.getFirstAttachment()?.type == Constants.AttachmentType.LOCATION.type
            }

            val textMessagesUnreadIds = textMessagesUnread.map { it.message.webId }
            val locationMessagesUnreadIds = locationMessagesUnread.map { it.message.webId }

            val listIds = mutableListOf<String>()
            listIds.addAll(textMessagesUnreadIds)
            listIds.addAll(locationMessagesUnreadIds)

            if (listIds.isNotEmpty()) {
                try {
                    val response = napoleonApi.sendMessagesRead(
                        MessagesReadReqDTO(
                            listIds
                        )
                    )

                    if (response.isSuccessful) {
                        messageLocalDataSource.updateMessageStatus(
                            listIds,
                            Constants.MessageStatus.READED.status
                        )
                    }
                } catch (ex: Exception) {
                    Timber.e(ex)
                }
            }
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

    /**
     * El channelPrivate debe ser sin el presence-
     */
    override fun readyForCall(contactId: Int, isVideoCall: Boolean, channelPrivate: String) {
        GlobalScope.launch {
            val readyForCallReqDTO = ReadyForCallReqDTO(
                contactId, isVideoCall, channelPrivate
            )

            val response = napoleonApi.readyForCall(readyForCallReqDTO)

            if (response.isSuccessful) {
                Timber.d("Usuario llamado")
            }
        }
    }

    override fun updateMessagesStatus(messagesWebIds: List<String>, state: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            messageLocalDataSource.updateMessageStatus(
                messagesWebIds,
                state
            )
        }
    }
}