package com.naposystems.napoleonchat.service.syncManager

import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.source.local.datasource.attachment.AttachmentLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.message.MessageLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.quoteMessage.QuoteLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.user.UserLocalDataSource
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.source.local.entity.QuoteEntity
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.contacts.ContactResDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.attachment.AttachmentResDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.call.CallContactReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.call.reject.RejectCallReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageResDTO
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessageDTO
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesReqDTO
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageDataEventRes
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageEventAttachmentRes
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageEventMessageRes
import com.naposystems.napoleonchat.utility.Constants
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SyncManagerImp @Inject constructor(
    private val cryptoMessage: CryptoMessage,
    private val napoleonApi: NapoleonApi,
    private val messageLocalDataSource: MessageLocalDataSource,
    private val attachmentLocalDataSource: AttachmentLocalDataSource,
    private val quoteLocalDataSource: QuoteLocalDataSource,
    private val contactLocalDataSource: ContactLocalDataSource,
    private val userLocalDataSource: UserLocalDataSource
) : SyncManager {

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

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

                                if (NapoleonApplication.currentConversationContactId != 0) {
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
            try {
                val response = napoleonApi.verifyMessagesReceived()

                if (response.isSuccessful) {

                    response.body()?.messagesId.let {
                        it?.let {
                            messageLocalDataSource.updateMessageStatus(
                                it,
                                Constants.MessageStatus.UNREAD.status
                            )
                        }
                    }

                    response.body()?.attachmentsId.let {
                        it?.let {
                            attachmentLocalDataSource.updateAttachmentStatus(
                                it,
                                Constants.AttachmentStatus.DOWNLOADING.status
                            )
                        }
                    }

                }
            } catch (e: java.lang.Exception) {
                Timber.e(e.localizedMessage)
            }
        }
    }

    override fun verifyMessagesRead() {
        GlobalScope.launch {
            try {
                val response = napoleonApi.verifyMessagesRead()

                if (response.isSuccessful) {

                    response.body()?.messagesId.let {
                        it?.let {
                            messageLocalDataSource.updateMessageStatus(
                                it,
                                Constants.MessageStatus.READED.status
                            )
                        }
                    }

                    response.body()?.attachmentsId.let {
                        it?.let {
                            attachmentLocalDataSource.updateAttachmentStatus(
                                it,
                                Constants.MessageStatus.READED.status
                            )
                        }
                    }

                }
            } catch (e: java.lang.Exception) {
                Timber.e(e.localizedMessage)
            }
        }
    }

    //TODO: Estos dos metodos tienen la misma funcion refactorizarlos
    override fun insertMessage(messageString: String) {

        Timber.d("**Paso 7: Proceso de Insercion del item $messageString")

        GlobalScope.launch() {

            Timber.d("**Paso 7.1: Desencriptar mensaje $messageString")

            try {
                val jsonAdapter: JsonAdapter<NewMessageEventMessageRes> =
                    moshi.adapter(NewMessageEventMessageRes::class.java)

                jsonAdapter.fromJson(messageString)
                    ?.let { newMessageEventMessageRes ->

                        if (newMessageEventMessageRes.messageType == Constants.MessageType.NEW_CONTACT.type) {
                            getRemoteContact()
                        }

                        val databaseMessage =
                            messageLocalDataSource.getMessageByWebId(
                                newMessageEventMessageRes.id,
                                false
                            )

                        Timber.d("**Paso 7.2: Validar WebId ${newMessageEventMessageRes.id}")


                        if (databaseMessage == null) {

                            val message =
                                newMessageEventMessageRes.toMessageEntity(Constants.IsMine.NO.value)

                            Timber.d("**Paso 7.3: Mensaje no existe WebId ${newMessageEventMessageRes.id}")

                            val messageId =
                                messageLocalDataSource.insertMessage(message)

                            Timber.d("**Paso 7.4: Mensaje insertado $messageId")

                            if (newMessageEventMessageRes.quoted.isNotEmpty()) {
                                Timber.d("**Paso 7.5.1: insertar Quote")
                                insertQuote(
                                    newMessageEventMessageRes.quoted,
                                    messageId.toInt()
                                )
                            }

                            val listAttachments =
                                NewMessageEventAttachmentRes.toListConversationAttachment(
                                    messageId.toInt(),
                                    newMessageEventMessageRes.attachments
                                )
                            if (listAttachments.isNotEmpty()) {
                                Timber.d("**Paso 7.5.2: insertar ${listAttachments.size} adjuntos")
                                attachmentLocalDataSource.insertAttachments(listAttachments)
                            }
                        }
                    }
            } catch (e: java.lang.Exception) {
                Timber.e("${e.localizedMessage} $messageString")
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

    override fun notifyMessageReceived(messagesReqDTO: MessagesReqDTO) {

        Timber.d("**Paso 9: Proceso consumir recibido del item $messagesReqDTO")

        GlobalScope.launch {
            try {
                Timber.d("**Paso 9.1: Proceso consumir recibido del item $messagesReqDTO")
                napoleonApi.notifyMessageReceived(messagesReqDTO)
            } catch (e: Exception) {
                Timber.e(e)
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

    override fun updateAttachmentsStatus(attachmentsWebIds: List<String>, state: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            attachmentLocalDataSource.updateAttachmentStatus(
                attachmentsWebIds,
                state
            )
            messageLocalDataSource.updateMessageStatusBeforeAttachment(
                attachmentsWebIds
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
                Timber.d("LLamada rechazada bb: ERROR AQUIIIII")
            }
        }
    }

    override fun existMessageById(id: String): Boolean = messageLocalDataSource.existMessage(id)

    override fun existAttachmentById(id: String): Boolean =
        attachmentLocalDataSource.existAttachment(id)

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

    suspend fun getContacts() {
        try {
            val response =
                napoleonApi.getContactsByState(Constants.FriendShipState.ACTIVE.state)

            if (response.isSuccessful) {

                val contactResDTO = response.body()!!

                val contacts = ContactResDTO.toEntityList(contactResDTO.contacts)

                val contactsToDelete =
                    contactLocalDataSource.insertOrUpdateContactList(contacts)

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

        GlobalScope.launch(Dispatchers.Default) {

            val messagesUnread =
                messageLocalDataSource.getTextMessagesByStatus(
                    NapoleonApplication.currentConversationContactId,
                    Constants.MessageStatus.UNREAD.status
                )

            val textMessagesUnread = messagesUnread
                .filter {
                    it.attachmentEntityList.isEmpty() ||
                            it.messageEntity.messageType == Constants.MessageType.MISSED_CALL.type ||
                            it.messageEntity.messageType == Constants.MessageType.MISSED_VIDEO_CALL.type
                }.map {
                    MessageDTO(
                        id = it.messageEntity.webId,
                        type = Constants.MessageTypeByStatus.MESSAGE.type,
                        user = it.messageEntity.contactId,
                        status = Constants.StatusMustBe.READED.status
                    )
                }

            val locationMessagesUnread = messagesUnread
                .filter {
                    it.getFirstAttachment()?.type == Constants.AttachmentType.LOCATION.type
                }.map {
                    MessageDTO(
                        id = it.messageEntity.webId,
                        type = Constants.MessageTypeByStatus.MESSAGE.type,
                        user = it.messageEntity.contactId,
                        status = Constants.StatusMustBe.READED.status
                    )
                }

            val messagesRead = mutableListOf<MessageDTO>()

            messagesRead.addAll(textMessagesUnread)

            messagesRead.addAll(locationMessagesUnread)

            if (messagesRead.isNotEmpty()) {
                try {

                    val messagesReqDTO = MessagesReqDTO(messagesRead)

                    val response = napoleonApi.sendMessagesRead(messagesReqDTO)

                    if (response.isSuccessful) {
                        messageLocalDataSource.updateMessageStatus(
                            messagesReqDTO.messages.map { it.id },
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

    override suspend fun getRemoteContact() {
        try {
            val response =
                napoleonApi.getContactsByState(Constants.FriendShipState.ACTIVE.state)

            if (response.isSuccessful) {

                val contactResDTO = response.body()!!

                val contacts = ContactResDTO.toEntityList(contactResDTO.contacts)

                val contactsToDelete =
                    contactLocalDataSource.insertOrUpdateContactList(contacts)

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
//            Timber.e(e)
        }
    }

    override fun getContact(contactId: Int): ContactEntity? {
        return contactLocalDataSource.getContactById(contactId)
    }

    override fun callContact(contact: Int, videoCall: Boolean, offer: String) {

        Timber.d("LLAMADA PASO 11 OUTGOING: Consumiendo llamando contacto")

        GlobalScope.launch(Dispatchers.IO) {
            val callContactReqDTO = CallContactReqDTO(
                contactToCall = contact,
                isVideoCall = videoCall,
                offer = offer
            )

            napoleonApi.callContact(callContactReqDTO)
        }
    }

    //
//
//    private fun validateMessageEvent(newMessageDataEventRes: NewMessageEventMessageRes) {
//        try {
//            val messages = arrayListOf(
//                ValidateMessage(
//                    id = newMessageDataEventRes.id,
//                    user = newMessageDataEventRes.userAddressee,
//                    status = Constants.MessageEventType.UNREAD.status
//                )
//            )
//
//            socketService.emitClientConversation(messages)
//
//        } catch (e: Exception) {
//            Timber.e(e)
//        }
//    }

//    override fun notifyMessageReceived_NOTIF(messageId: String) {
//        GlobalScope.launch {
//            try {
//                val messageReceivedReqDTO = MessageReceivedReqDTO(messageId)
//                napoleonApi.notifyMessageReceived(messageReceivedReqDTO)
//            } catch (e: Exception) {
////                    Timber.e(e)
//            }
//        }
//    }

//    override fun getIsOnCallPref() = NapoleonApplication.isOnCall

//    override fun getContactSilenced(contactId: Int, silenced: (Boolean?) -> Unit) {
//        GlobalScope.launch {
//            withContext(Dispatchers.IO) {
//                silenced(contactLocalDataSource.getContactSilenced(contactId))
//            }
//        }
//    }


}