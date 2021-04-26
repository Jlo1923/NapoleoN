package com.naposystems.napoleonchat.service.syncManager

import android.util.Log
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.model.toMessagesReqDTO
import com.naposystems.napoleonchat.model.toMessagesReqDTOFromRelation
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
import com.naposystems.napoleonchat.utility.Constants.StatusMustBe
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class SyncManagerImp @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val cryptoMessage: CryptoMessage,
    private val messageLocalDataSource: MessageLocalDataSource,
    private val attachmentLocalDataSource: AttachmentLocalDataSource,
    private val quoteLocalDataSource: QuoteLocalDataSource,
    private val contactLocalDataSource: ContactLocalDataSource,
    private val userLocalDataSource: UserLocalDataSource
) : SyncManager {

    val queueNewMessageDataEventRes: Queue<NewMessageDataEventRes> =
        LinkedList<NewMessageDataEventRes>()

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
                    val messageResList = response.body()!!.toMutableList()
                    if (messageResList.isNotEmpty()) {
                        messageResList.forEach { messageRes ->
                            handlerMessage(messageRes)
                        }
                        handlerNotify(messageResList, contactId)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    @Synchronized
    suspend fun handlerMessage(messageRes: MessageResDTO) {

        val databaseMessage = messageLocalDataSource.getMessageByWebId(messageRes.id, false)

        //Validar si existe mensaje
        if (databaseMessage == null) {

            val message = MessageResDTO.toMessageEntity(null, messageRes, Constants.IsMine.NO.value)

            val messageId = messageLocalDataSource.insertMessage(message)

            Timber.d("Conversation insertó mensajes")

            if (messageRes.quoted.isNotEmpty())
                insertQuote(messageRes.quoted, messageId.toInt())

            handlerAttachments(messageRes.attachments, messageId.toInt())

        } else {
            handlerAttachments(messageRes.attachments, databaseMessage.messageEntity.id)
        }

    }

    @Synchronized
    suspend fun handlerAttachments(
        attachments: List<AttachmentResDTO>,
        messageId: Int
    ) {
        val listAttachments = AttachmentResDTO.toListConversationAttachment(
            messageId,
            attachments
        )

        /**
         * Solo hacemos insersion de attachments sino existe
         * por medio de su id
         */
        listAttachments.forEach { attachment ->
            attachmentLocalDataSource.apply {
                if (this.existAttachment(attachment.id.toString()).not()) {
                    this.insertAttachments(listOf(attachment))
                }
            }
        }
    }

    @Synchronized
    suspend fun handlerNotify(listMessages: MutableList<MessageResDTO>, contactId: Int?) {

        val listMessagesNotify: MutableList<MessageAttachmentRelation> = mutableListOf()

        listMessages.forEach { messsageRes ->
            messsageRes.id?.let { message_id_web ->

                val messageAttachmentRelation =
                    messageLocalDataSource.getMessageByWebId(message_id_web, false)

                if (messageAttachmentRelation != null)
                    listMessagesNotify.add(messageAttachmentRelation)
            }
        }

        notifyMessageReceived(listMessagesNotify.toMessagesReqDTOFromRelation(StatusMustBe.RECEIVED))

//        Timber.d("Conversation insertó attachment")
//        if (NapoleonApplication.currentConversationContactId != 0) {
//            notifyMessagesReaded()
//        }
        contactId?.let { RxBus.publish(RxEvent.NewMessageEventForCounter(contactId)) }
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
                        }
                    }
            } catch (e: java.lang.Exception) {
                Timber.e("${e.localizedMessage} $messageString")
            }

        }
    }

    override fun insertNewMessage(newMessageDataEventRes: NewMessageDataEventRes) {
        if (queueNewMessageDataEventRes.isEmpty()) {
            queueNewMessageDataEventRes.add(newMessageDataEventRes)
            tryHandleNextItemInQueue()
        } else {
            queueNewMessageDataEventRes.add(newMessageDataEventRes)
        }

    }

    private fun tryHandleNextItemInQueue() {
        GlobalScope.launch {

            val element = if (queueNewMessageDataEventRes.isEmpty().not()) {
                queueNewMessageDataEventRes.first()
            } else {
                null
            }

            element?.let {

                Timber.d("insertNewMessage element.messageId $element.messageId")

                val databaseMessage =
                    messageLocalDataSource.getMessageByWebId(element.messageId, false)

                /**
                 * Si el mensaje no existe dejamos el proceso como estaba, en caso contrario,
                 * solo tomaremos el attachment que acompaña el mensaje que nos llega para agregarlo
                 * a la database local
                 */
                try {
                    databaseMessage?.let {
                        Timber.d("insertNewMessage getMessageIdAndSaveAttachmentLocally")
                        getMessageIdAndSaveAttachmentLocally(element, it.messageEntity.id)
                    } ?: run {
                        Timber.d("insertNewMessage insertNewMessageAndAttachmentLocally")
                        insertNewMessageAndAttachmentLocally(element)
                    }
                    queueNewMessageDataEventRes.poll()
                    tryHandleNextItemInQueue()
                } catch (exception: Exception) {
                    Timber.d("syncManager.insertNewMessage")
                    exception.printStackTrace()
                }
            }
        }
    }

    private suspend fun getMessageIdAndSaveAttachmentLocally(
        newMessageDataEventRes: NewMessageDataEventRes,
        idMessage: Int
    ) {
        Log.i("JkDev", "getMessageIdAndSaveAttachmentLocally: $idMessage")
        val newMessageEventData = if (BuildConfig.ENCRYPT_API) {
            cryptoMessage.decryptMessageBody(newMessageDataEventRes.message)
        } else {
            newMessageDataEventRes.message
        }

        val jsonAdapter =
            Moshi.Builder().build().adapter(NewMessageEventMessageRes::class.java)

        jsonAdapter.fromJson(newMessageEventData)?.let { newMessageEventMessageRes ->
            if (newMessageEventMessageRes.quoted.isNotEmpty()) {
                insertQuote(newMessageEventMessageRes.quoted, idMessage)
            }
            val listAttachments =
                NewMessageEventAttachmentRes.toListConversationAttachment(
                    idMessage,
                    newMessageEventMessageRes.attachments
                )
            attachmentLocalDataSource.insertAttachments(listAttachments)

            val listMessagesToReceived = listOf(
                newMessageEventMessageRes
            ).toMessagesReqDTO(StatusMustBe.RECEIVED)

            //notifyMessageReceived(listMessagesToReceived)

            //TODO: JuankDev12 tambien hay que emitir por sokect aqui solo esta emitiendo por notificacion
            // en el SocketClientImp se hace la emisión por tanto este proceso deberia hacerse allá

            RxBus.publish(RxEvent.NewMessageEventForCounter(newMessageDataEventRes.contactId))
        }
    }

    private suspend fun insertNewMessageAndAttachmentLocally(
        newMessageDataEventRes: NewMessageDataEventRes
    ) {
        getContacts()
        val newMessageEventData = if (BuildConfig.ENCRYPT_API) {
            cryptoMessage.decryptMessageBody(newMessageDataEventRes.message)
        } else {
            newMessageDataEventRes.message
        }

        val jsonAdapter =
            Moshi.Builder().build().adapter(NewMessageEventMessageRes::class.java)

        jsonAdapter.fromJson(newMessageEventData)?.let { newMessageEventMessageRes ->
            val message = newMessageEventMessageRes.toMessageEntity(Constants.IsMine.NO.value)
            val messageId = messageLocalDataSource.insertMessage(message)
            Log.i("JkDev", "Insertamos attachment desde creacion: $messageId")
            if (newMessageEventMessageRes.quoted.isNotEmpty()) {
                insertQuote(newMessageEventMessageRes.quoted, messageId.toInt())
            }
            val listAttachments =
                NewMessageEventAttachmentRes.toListConversationAttachment(
                    messageId.toInt(),
                    newMessageEventMessageRes.attachments
                )
            attachmentLocalDataSource.insertAttachments(listAttachments)

            val listMessagesToReceived = listOf(
                newMessageEventMessageRes
            ).toMessagesReqDTO(StatusMustBe.RECEIVED)

            //notifyMessageReceived(listMessagesToReceived)

            //TODO: JuankDev12 tambien hay que emitir por sokect aqui solo esta emitiendo por notificacion
            // en el SocketClientImp se hace la emisión por tanto este proceso deberia hacerse allá

            RxBus.publish(RxEvent.NewMessageEventForCounter(newMessageDataEventRes.contactId))
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
        attachmentsWebIds.forEach {
            Timber.d("updateAttachmentsStatus id: ${it}")
        }

        Timber.d("updateAttachmentsStatus State: ${state}")

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
            if (response.isSuccessful) {
                response.body()?.messagesId.let {
                    it?.let {
                        messageLocalDataSource.deletedMessages(
                            it
                        )
                    }
                }

                response.body()?.attachmentsId.let {
                    it?.let {
                        attachmentLocalDataSource.deletedAttachments(
                            it
                        )
                    }
                }
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

    @Synchronized
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
                        status = StatusMustBe.READED.status
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
                        status = StatusMustBe.READED.status
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