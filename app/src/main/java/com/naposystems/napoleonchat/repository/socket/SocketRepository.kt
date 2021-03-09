package com.naposystems.napoleonchat.repository.socket

import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.source.local.datasource.attachment.AttachmentLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.message.MessageLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.quoteMessage.QuoteLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.user.UserLocalDataSource
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.source.local.entity.QuoteEntity
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.contacts.ContactResDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.attachment.AttachmentResDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.call.reject.RejectCallReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageReceivedReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageResDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessagesReadReqDTO
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageDataEventRes
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageEventAttachmentRes
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageEventMessageRes
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Data
import com.naposystems.napoleonchat.webService.socket.IContractSocketService
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class SocketRepository @Inject constructor(
    private val cryptoMessage: CryptoMessage,
    private val napoleonApi: NapoleonApi,
    private val messageLocalDataSource: MessageLocalDataSource,
    private val attachmentLocalDataSource: AttachmentLocalDataSource,
    private val quoteLocalDataSource: QuoteLocalDataSource,
    private val contactLocalDataSource: ContactLocalDataSource,
    private val userLocalDataSource: UserLocalDataSource
) : IContractSocketService.Repository {

    //region Metodos De La Interface
    override fun getUserId(): Int {

        val user = userLocalDataSource.getMyUser()

        if (user != null)
            return user.id
        else
            return Constants.UserNotExist.USER_NO_EXIST.user

    }

    override fun getMyMessages(contactId: Int?) {

        GlobalScope.launch(Dispatchers.Main) {
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

    override fun insertNewMessage(newMessageDataEventRes: NewMessageDataEventRes) {
        GlobalScope.launch {

            getContacts()

            val databaseMessage =
                messageLocalDataSource.getMessageByWebId(
                    newMessageDataEventRes.messageId,
                    false
                )

            if (databaseMessage == null) {

                val newMessageEventMessageResData: String = if (BuildConfig.ENCRYPT_API) {
                    cryptoMessage.decryptMessageBody(newMessageDataEventRes.message)
                } else {
                    newMessageDataEventRes.message
                }
                val moshi = Moshi.Builder().build()
                val jsonAdapter: JsonAdapter<NewMessageEventMessageRes> =
                    moshi.adapter(NewMessageEventMessageRes::class.java)

                jsonAdapter.fromJson(newMessageEventMessageResData)
                    ?.let { newMessageEventMessageRes ->
                        val message = newMessageEventMessageRes.toMessageEntity(
                            Constants.IsMine.NO.value
                        )

                        val messageId = messageLocalDataSource.insertMessage(message)
                        Timber.d("Conversation insertó mensajes")

                        if (newMessageEventMessageRes.quoted.isNotEmpty()) {
                            insertQuote(newMessageEventMessageRes.quoted, messageId.toInt())
                        }

                        val listAttachments =
                            NewMessageEventAttachmentRes.toListConversationAttachment(
                                messageId.toInt(),
                                newMessageEventMessageRes.attachments
                            )

                        attachmentLocalDataSource.insertAttachments(listAttachments)
                        Timber.d("Conversation insertó attachment")

                        RxBus.publish(
                            RxEvent.NewMessageEventForCounter(newMessageDataEventRes.contactId)
                        )
                    }
            }
        }
    }

    override fun notifyMessageReceived(messageId: String) {
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

    override fun updateMessagesStatus(messagesWebIds: List<String>, state: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            messageLocalDataSource.updateMessageStatus(
                messagesWebIds,
                state
            )
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

    override fun deleteContact(contactId: Int?) {
        GlobalScope.launch {
            contactId?.let {
                contactLocalDataSource.getContactById(contactId)?.let { contact ->
                    RxBus.publish(RxEvent.DeleteChannel(contact))
                    contactLocalDataSource.deleteContact(contact)
                }
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

    override fun existIdMessage(id: String): Boolean = messageLocalDataSource.existMessage(id)

    override fun validateMessageType(messagesWebIds: List<String>, state: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            val listWebId = mutableListOf<String>()
            for (webId in messagesWebIds) {
                val localMessage = messageLocalDataSource.getMessageByWebId(webId, false)

                localMessage?.let {
                    if (it.attachmentEntityList.count() == 0) {
                        Timber.d("*TestMessageEvent: empty attachment")
                        listWebId.add(it.messageEntity.webId)
                    } else {
                        validateAttachmentType(it, listWebId)
                    }
                }
            }

            updateMessagesStatus(listWebId, state)
        }
    }
    //endregion

    //region Metodos Privados
    suspend fun getContacts() {
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

                        RxBus.publish(RxEvent.DeleteChannel(contact))

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

    private suspend fun insertQuote(quoteWebId: String, messageId: Int) {

        val originalMessage =
            messageLocalDataSource.getMessageByWebId(quoteWebId, false)

        if (originalMessage != null) {

            var firstAttachmentEntity: AttachmentEntity? = null

            if (originalMessage.attachmentEntityList.isNotEmpty()) {
                firstAttachmentEntity = originalMessage.attachmentEntityList.first()
            }

            val quote = QuoteEntity(
                id = 0,
                messageId = messageId,
                contactId = originalMessage.messageEntity.contactId,
                body = originalMessage.messageEntity.body,
                attachmentType = firstAttachmentEntity?.type ?: "",
                thumbnailUri = firstAttachmentEntity?.fileName ?: "",
                messageParentId = originalMessage.messageEntity.id,
                isMine = originalMessage.messageEntity.isMine
            )

            quoteLocalDataSource.insertQuote(quote)
        }
    }

    private fun notifyMessagesReaded() {

        Timber.d("*notifyMessageRead: Socket")

        GlobalScope.launch(Dispatchers.IO) {

            val messagesUnread =
                messageLocalDataSource.getTextMessagesByStatus(
                    Data.contactId,
                    Constants.MessageStatus.UNREAD.status
                )

            val textMessagesUnread = messagesUnread.filter {
                it.attachmentEntityList.isEmpty() ||
                        it.messageEntity.messageType == Constants.MessageType.MISSED_CALL.type ||
                        it.messageEntity.messageType == Constants.MessageType.MISSED_VIDEO_CALL.type
            }

            val locationMessagesUnread = messagesUnread.filter {
                it.getFirstAttachment()?.type == Constants.AttachmentType.LOCATION.type
            }

            val textMessagesUnreadIds = textMessagesUnread.map { it.messageEntity.webId }
            val locationMessagesUnreadIds = locationMessagesUnread.map { it.messageEntity.webId }

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

    private fun validateAttachmentType(
        messageAttachmentRelation: MessageAttachmentRelation,
        listWebId: MutableList<String>
    ) {
        for (attachment in messageAttachmentRelation.attachmentEntityList) {
            when (attachment.type) {
                Constants.AttachmentType.GIF.type,
                Constants.AttachmentType.GIF_NN.type,
                Constants.AttachmentType.LOCATION.type,
                Constants.AttachmentType.DOCUMENT.type -> {
                    listWebId.add(messageAttachmentRelation.messageEntity.webId)
                }
            }
        }
    }
    //endregion

//    /**
//     * El channelPrivate debe ser sin el presence-
//     */
//    override fun readyForCall(contactId: Int, isVideoCall: Boolean, channelPrivate: String) {
//        GlobalScope.launch {
//            val readyForCallReqDTO = ReadyForCallReqDTO(
//                contactId, isVideoCall, channelPrivate
//            )
//
//            val response = napoleonApi.readyForCall(readyForCallReqDTO)
//
//            if (response.isSuccessful) {
//                Timber.d("Usuario llamado")
//            }
//        }
//    }

}
