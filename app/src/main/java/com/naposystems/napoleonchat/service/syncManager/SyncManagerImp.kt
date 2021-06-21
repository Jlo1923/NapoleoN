package com.naposystems.napoleonchat.service.syncManager

import android.util.Log
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.model.toMessagesReqDTO
import com.naposystems.napoleonchat.model.toMessagesReqDTOFromRelation
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.socketClient.GetMessagesSocketListener
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
import com.naposystems.napoleonchat.source.remote.dto.cancelCall.CancelCallReqDTO
import com.naposystems.napoleonchat.source.remote.dto.contacts.ContactResDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.attachment.AttachmentResDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.call.CallContactReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.call.reject.RejectCallReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageResDTO
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessageAndAttachmentResDTO
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessageDTO
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesReqDTO
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageDataEventRes
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageEventAttachmentRes
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageEventMessageRes
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Constants.MessageStatus.READED
import com.naposystems.napoleonchat.utility.Constants.StatusMustBe
import com.naposystems.napoleonchat.utility.Utils
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

    private var getMessagesSocketListener: GetMessagesSocketListener? = null

    override fun getUserId(): Int {

        val user = userLocalDataSource.getMyUser()

        if (user != null)
            return user.id
        else
            return Constants.UserNotExist.USER_NO_EXIST.user

    }

    override fun setGetMessagesSocketListener(
        getMessagesSocketListener: GetMessagesSocketListener
    ) {
        this.getMessagesSocketListener = getMessagesSocketListener
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

            handlerAttachments(messageRes.attachments, messageId.toInt(), message.contactId)

        } else {
            handlerAttachments(
                messageRes.attachments,
                databaseMessage.messageEntity.id,
                databaseMessage.messageEntity.contactId
            )
        }

    }

    @Synchronized
    suspend fun handlerAttachments(
        attachments: List<AttachmentResDTO>,
        messageId: Int,
        contactId: Int
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
                if (this.existAttachmentByWebId(attachment.webId).not()) {
                    this.insertAttachments(listOf(attachment))
                }

                val listUniqueAttachment = MessageDTO(
                    id = attachment.webId,
                    type = Constants.MessageType.ATTACHMENT.type,
                    user = contactId,
                    status = StatusMustBe.RECEIVED.status
                )
                notifyMessageReceivedRemote(MessagesReqDTO(listOf(listUniqueAttachment)))
            }
        }
    }

    @Synchronized
    suspend fun handlerNotify(listMessages: MutableList<MessageResDTO>, contactId: Int?) {

        val listMessagesNotify: MutableList<MessageAttachmentRelation> = mutableListOf()

        listMessages.forEach { messsageRes ->

            messsageRes.id.let { messageIdWeb ->

                val messageAttachmentRelation =
                    messageLocalDataSource.getMessageByWebId(messageIdWeb, false)

                if (messageAttachmentRelation != null) {
                    if (messageAttachmentRelation.messageEntity.numberAttachments <= 1) {
                        listMessagesNotify.add(messageAttachmentRelation)
                    }
                }
            }
        }

        val listMessagesReceived =
            listMessagesNotify.toMessagesReqDTOFromRelation(StatusMustBe.RECEIVED)

        notifyMessageReceivedRemote(listMessagesReceived)

        getMessagesSocketListener?.emitSocketClientConversation(listMessagesReceived)

        contactId?.let { RxBus.publish(RxEvent.NewMessageEventForCounter(contactId)) }
    }

    override fun verifyMessagesReceived() {
        GlobalScope.launch {
            try {
                val response = napoleonApi.verifyMessagesReceived()

                if (response.isSuccessful) {

                    response.body()?.let { handleMessagesReceived(it) }

                    response.body()?.attachmentsId.let {
                        it?.let {
                            if (it.isNotEmpty()) {
                                attachmentLocalDataSource.updateAttachmentStatus(
                                    it,
                                    Constants.AttachmentStatus.RECEIVED.status
                                )
                            }
                        }
                    }

                }
            } catch (e: java.lang.Exception) {
                Timber.e(e.localizedMessage)
            }
        }
    }

    private suspend fun handleMessagesReceived(data: MessageAndAttachmentResDTO) {
        if (data.messagesId.isNotEmpty()) {
            /**
             * debemos validar los attachments del mensaje padre, como lo vamos a marcar como recibidos
             * tomamos todos los attachments y los marcamos
             */
            data.messagesId.forEach {
                val message = messageLocalDataSource.getMessageByWebId(it, false)
                val webIds = message?.attachmentEntityList?.map { it.webId }
                webIds?.let {
                    attachmentLocalDataSource.updateAttachmentStatus(
                        it,
                        Constants.AttachmentStatus.RECEIVED.status
                    )
                }
            }
            messageLocalDataSource.updateMessageStatus(data.messagesId, READED.status)
        }
    }

    override fun verifyMessagesRead() {
        GlobalScope.launch {
            try {
                val response = napoleonApi.verifyMessagesRead()
                if (response.isSuccessful) {
                    response.body()?.let {
                        handleDataAttachmentsRead(it)
                        handleDataMessagesRead(it)
                    }
                }
            } catch (e: java.lang.Exception) {
                Timber.e(e.localizedMessage)
            }
        }
    }

    @Synchronized
    private suspend fun handleDataAttachmentsRead(it: MessageAndAttachmentResDTO) {
        if (it.attachmentsId.isEmpty().not()) {
            attachmentLocalDataSource.updateAttachmentStatus(
                it.attachmentsId, READED.status
            )
        }
    }

    @Synchronized
    private suspend fun handleDataMessagesRead(data: MessageAndAttachmentResDTO) {
        if (data.messagesId.isNotEmpty()) {
            /**
             * debemos validar los attachments del mensaje padre, como lo vamos a marcar como leidos
             * tomamos todos los attachments y los marcamos
             */
            data.messagesId.forEach {
                val message = messageLocalDataSource.getMessageByWebId(it, false)
                val webIds = message?.attachmentEntityList?.map { it.webId }
                webIds?.let {
                    attachmentLocalDataSource.updateAttachmentStatus(
                        it,
                        Constants.AttachmentStatus.READED.status
                    )
                }
            }
            messageLocalDataSource.updateMessageStatus(data.messagesId, READED.status)
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

                        if (newMessageEventMessageRes.messageType == Constants.MessageTextType.NEW_CONTACT.type) {
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

    override fun notifyMessageReceivedRemote(messagesReqDTO: MessagesReqDTO) {

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

    override fun updateMessagesStatus(
        messagesWebIds: List<String>,
        state: Int
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            messageLocalDataSource.updateMessageStatus(messagesWebIds, state)
        }
    }

    override fun updateAttachmentsStatus(attachmentsWebIds: List<String>, state: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            if (attachmentsWebIds.isNotEmpty()) {
                attachmentLocalDataSource.updateAttachmentStatus(
                    attachmentsWebIds, state
                )
            }

            if (attachmentsWebIds.isNotEmpty()) {
                messageLocalDataSource.updateMessageStatusBeforeAttachment(
                    attachmentsWebIds
                )
            }
        }
    }

    override fun getDeletedMessages() {
        GlobalScope.launch {
            val response = napoleonApi.getDeletedMessages()
            if (response.isSuccessful) {
                response.body()?.messagesId.let {
                    it?.let { messageLocalDataSource.deleteMessagesByWebId(it) }
                }

                response.body()?.attachmentsId.let {
                    it?.let { listIds ->
                        if (listIds.isNotEmpty()) {
                            attachmentLocalDataSource.deletedAttachments(listIds)
                        }
                    }
                }
            }
        }
    }

    private suspend fun decreaseAmountAttachments(attachmentWebId: String) {
        val theAttachment =
            attachmentLocalDataSource.getAttachmentByWebId(attachmentWebId)
        theAttachment?.let {
            val msg =
                messageLocalDataSource.getMessageByWebId(it.messageWebId, false)
            msg?.messageEntity?.let { messageEntity ->
                val msgCopy =
                    messageEntity.copy(numberAttachments = messageEntity.numberAttachments - 1)
                messageLocalDataSource.updateMessage(msgCopy)
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

    override fun sendMissedCall() {
        //TODO: Revisar tiempo de autodestruccion de este mensaje
        GlobalScope.launch {
            try {
                NapoleonApplication.callModel?.let { callModel ->
                    val messageReqDTO = MessageReqDTO(
                        userDestination = callModel.contactId,
                        quoted = "",
                        body = "",
                        numberAttachments = 0,
                        destroy = Constants.SelfDestructTime.EVERY_ONE_DAY.time,
                        messageType = if (callModel.isVideoCall) Constants.MessageTextType.MISSED_VIDEO_CALL.type else Constants.MessageTextType.MISSED_CALL.type,
                        uuidSender = UUID.randomUUID().toString()
                    )
                    napoleonApi.sendMessage(messageReqDTO)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    override fun rejectCall() {
        NapoleonApplication.callModel?.let { callModel ->
            rejectCall(
                callModel.contactId,
                callModel.channelName
            )
        }
    }

    override fun rejectCall(contactId: Int, channelName: String) {
        GlobalScope.launch {
            val rejectCallReqDTO = RejectCallReqDTO(
                contactId = contactId,
                channel = channelName
            )
            napoleonApi.rejectCall(rejectCallReqDTO)
        }
    }

    override fun cancelCall() {
        GlobalScope.launch {
            NapoleonApplication.callModel?.let { callModel ->
                val cancelCallReqDTO = CancelCallReqDTO(
                    contactId = callModel.contactId,
                    channel = callModel.channelName
                )
                napoleonApi.cancelCall(cancelCallReqDTO)
            }
        }
    }

    override fun existMessageById(id: String): Boolean = messageLocalDataSource.existMessage(id)

    override fun existAttachmentById(id: String): Boolean =
        attachmentLocalDataSource.existAttachmentByWebId(id)

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

            if (listWebId.isNotEmpty()) {
                updateMessagesStatus(listWebId, state)
            }

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
                        messageLocalDataSource.deleteMessageByContactIdAndType(
                            contact.id,
                            Constants.MessageTextType.NEW_CONTACT.type
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

    override fun notifyMessagesReaded() {

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
                            it.messageEntity.messageType == Constants.MessageTextType.MISSED_CALL.type ||
                            it.messageEntity.messageType == Constants.MessageTextType.MISSED_VIDEO_CALL.type
                }.map {
                    MessageDTO(
                        id = it.messageEntity.webId,
                        type = Constants.MessageType.TEXT.type,
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
                        type = Constants.MessageType.TEXT.type,
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
                            READED.status
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
                        messageLocalDataSource.deleteMessageByContactIdAndType(
                            contact.id,
                            Constants.MessageTextType.NEW_CONTACT.type
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

    override fun callContact() {
        Timber.d("LLAMADA PASO 11 OUTGOING: Consumiendo llamando contacto")
        GlobalScope.launch(Dispatchers.IO) {
            NapoleonApplication.callModel?.let { callModel ->
                val callContactReqDTO = CallContactReqDTO(
                    contactToCall = callModel.contactId,
                    isVideoCall = callModel.isVideoCall,
                    offer = Utils.encoderOffer(callModel.offer)
                )
                napoleonApi.callContact(callContactReqDTO)
            }
        }
    }

    override fun tryMarkMessageParentAsReceived(idsAttachments: List<String>) {
        GlobalScope.launch(Dispatchers.IO) {
            idsAttachments.forEach { idAttachmentString ->
                val theAttach = attachmentLocalDataSource.getAttachmentByWebId(idAttachmentString)
                theAttach?.let { attachment ->
                    val theMsg =
                        messageLocalDataSource.getMessageByWebId(attachment.messageWebId, false)
                    theMsg?.let { msgAndRelation ->
                        val filter =
                            msgAndRelation.attachmentEntityList.filter { it.isReceived() || it.isDownloadComplete() }
                        if (filter.size == msgAndRelation.messageEntity.numberAttachments) {
                            updateMessagesStatus(
                                listOf(msgAndRelation.messageEntity.webId),
                                Constants.MessageStatus.UNREAD.status
                            )

                            val messageDTO = MessageDTO(
                                id = msgAndRelation.messageEntity.webId,
                                type = Constants.MessageType.TEXT.type,
                                user = msgAndRelation.messageEntity.contactId,
                                status = StatusMustBe.RECEIVED.status
                            )
                            val list = MessagesReqDTO(listOf(messageDTO))
                            napoleonApi.notifyMessageReceived(list)

                        }
                    }
                }
            }
        }
    }

    /**
     * Debemos validar que no este marcado como leido
     */
    override fun tryMarkMessageParentAsRead(idsAttachments: List<String>) {
        GlobalScope.launch(Dispatchers.IO) {
            idsAttachments.forEach { idAttachmentString ->
                val theAttach = attachmentLocalDataSource.getAttachmentByWebId(idAttachmentString)
                theAttach?.let { attachment ->
                    val theMsg =
                        messageLocalDataSource.getMessageByWebId(attachment.messageWebId, false)
                    theMsg?.let { msgAndRelation ->
                        if (msgAndRelation.messageEntity.isRead().not()) {
                            val filter =
                                msgAndRelation.attachmentEntityList.filter { it.isRead() }
                            if (filter.size == msgAndRelation.attachmentEntityList.size) {
                                setMsgReadLocallyAndRemotely(msgAndRelation)
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun setMsgReadLocallyAndRemotely(msgAndRelation: MessageAttachmentRelation) {
        updateMessagesStatus(
            listOf(msgAndRelation.messageEntity.webId),
            READED.status
        )

        val messageDTO = MessageDTO(
            id = msgAndRelation.messageEntity.webId,
            type = Constants.MessageType.TEXT.type,
            user = msgAndRelation.messageEntity.contactId,
            status = StatusMustBe.READED.status
        )
        val list = MessagesReqDTO(listOf(messageDTO))
        napoleonApi.sendMessagesRead(list)
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