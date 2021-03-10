package com.naposystems.napoleonchat.service.syncManager

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
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
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
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class SyncManagerImp @Inject constructor(
    private val cryptoMessage: CryptoMessage,
    private val napoleonApi: NapoleonApi,
//    private val socketService: SocketService,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val messageLocalDataSource: MessageLocalDataSource,
    private val attachmentLocalDataSource: AttachmentLocalDataSource,
    private val quoteLocalDataSource: QuoteLocalDataSource,
    private val contactLocalDataSource: ContactLocalDataSource,
    private val userLocalDataSource: UserLocalDataSource
) : SyncManager {

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    init {

        Timber.d("Pusher: //////////////////////////////////////")

    }

//    override fun getSocketId(): String {
//        return if (pusher.connection.state == ConnectionState.CONNECTED) {
//            pusher.connection.socketId
//        } else {
//            Constants.SocketIdNotExist.SOCKET_ID_NO_EXIST.socket
//        }
//    }

//    override fun getStatusGlobalChannel(): Boolean {
//        return globalChannel.isSubscribed
//    }
//
//    private fun emitClientConversation(messages: List<ValidateMessage>) {
//
//        try {
//
//            val validateMessage = ValidateMessageEventDTO(messages)
//
//            val adapterValidate = moshi.adapter(ValidateMessageEventDTO::class.java)
//
//            val jsonObject = adapterValidate.toJson(validateMessage)
//
//            if (jsonObject.isNotEmpty())
//                globalChannel.trigger(SocketServiceImp.CLIENT_CONVERSATION_NN, jsonObject)
//
//        } catch (e: Exception) {
//            Timber.e(e)
//        }
//
//    }
    //region Implementacion Socket Mensajes
//    override fun connectSocket() {
//
//        Timber.d("Instance From Sync: $pusher")
//
//        Timber.d("Pusher: *****************")
//
//        Timber.d("Pusher: connectSocket: State:${pusher.connection.state}")
//
//        if (getUserId() != Constants.UserNotExist.USER_NO_EXIST.user) {
//
//            if (pusher.connection.state == ConnectionState.DISCONNECTED ||
//                pusher.connection.state == ConnectionState.DISCONNECTING
//            ) {
//
//                pusher.connect(object : ConnectionEventListener {
//
//                    override fun onConnectionStateChange(change: ConnectionStateChange?) {
//
//                        if (change?.currentState == ConnectionState.CONNECTED) {
//
//                            pusher.unsubscribe(privateGlobalChannelName)
//
//                            subscribeToPrivateGlobalChannel()
//
//                        } else
//                            Timber.d("Pusher: connectSocket: State:${pusher.connection.state}")
//
//                    }
//
//                    override fun onError(message: String?, code: String?, e: java.lang.Exception?) {
//
//                        Timber.d("Pusher: connectSocket: onError $message, code: $code")
//
//                        pusher.connect()
//
//                    }
//
//                })
//            }
//        }
//    }

    //region Metodos Privados
//    private fun subscribeToPrivateGlobalChannel() {
//
//        try {
//            globalChannel = pusher.subscribePrivate(
//                privateGlobalChannelName,
//                object : PrivateChannelEventListener {
//                    override fun onEvent(event: PusherEvent?) {
//                        Timber.d("Pusher: subscribeToPrivateGlobalChannel: onEvent ${event?.data}")
//                    }
//
//                    override fun onAuthenticationFailure(
//                        message: String?,
//                        e: java.lang.Exception?
//                    ) {
//                        Timber.d("Pusher: subscribeToPrivateGlobalChannel: onAuthenticationFailure")
//                    }
//
//                    override fun onSubscriptionSucceeded(channelName: String?) {
//
//                        Timber.d("Pusher: subscribeToPrivateGlobalChannel: onSubscriptionSucceeded:$channelName")
//
//                    }
//                }
//            )
//        } catch (e: Exception) {
//            Timber.e("Pusher:  subscribeToPrivateGlobalChannel: Exception: $e")
//        }
//    }

    //region SocketService
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

    //TODO: Estos dos metodos tienen la misma funcion refactorizarlos
    override fun insertMessage(messageString: String) {

        Timber.d(
            "Paso 4: voy a insertar el mensaje $messageString"
        )

        GlobalScope.launch(Dispatchers.IO) {
            val newMessageEventMessageResData: String = if (BuildConfig.ENCRYPT_API) {
                cryptoMessage.decryptMessageBody(messageString)
            } else {
                messageString
            }


            Timber.d("Paso 5: Desencriptar mensaje $messageString")
            try {
                val jsonAdapter: JsonAdapter<NewMessageEventMessageRes> =
                    moshi.adapter(NewMessageEventMessageRes::class.java)

                jsonAdapter.fromJson(newMessageEventMessageResData)
                    ?.let { newMessageEventMessageRes ->

                        if (newMessageEventMessageRes.messageType == Constants.MessageType.NEW_CONTACT.type) {
                            getRemoteContact()
                        }

//                    validateMessageEvent(newMessageEventMessageRes)

                        val databaseMessage =
                            messageLocalDataSource.getMessageByWebId(
                                newMessageEventMessageRes.id,
                                false
                            )


                        Timber.d("Paso 6: Validar WebId ${newMessageEventMessageRes.id}")

                        if (databaseMessage == null) {

                            val message =
                                newMessageEventMessageRes.toMessageEntity(Constants.IsMine.NO.value)

//                    if (BuildConfig.ENCRYPT_API) {
//                        message.encryptBody(cryptoMessage)
//                    }

                            Timber.d("Paso 7: Mensaje No Existia $databaseMessage")

                            val messageId =
                                messageLocalDataSource.insertMessage(message)

                            Timber.d("Paso 8: Aqui inserto eso  $messageId")

                            if (newMessageEventMessageRes.quoted.isNotEmpty()) {
                                insertQuote_NOTIF(
                                    newMessageEventMessageRes.quoted,
                                    messageId.toInt()
                                )
                            }

                            val listAttachments =
                                NewMessageEventAttachmentRes.toListConversationAttachment(
                                    messageId.toInt(),
                                    newMessageEventMessageRes.attachments
                                )

                            attachmentLocalDataSource.insertAttachments(listAttachments)
                        }
                    }
            } catch (e: java.lang.Exception) {
                Timber.e("${e.localizedMessage} $newMessageEventMessageResData")
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
            try {
                val messageReceivedReqDTO = MessageReceivedReqDTO(messageId)
                napoleonApi.notifyMessageReceived(messageReceivedReqDTO)
                Timber.d("notifyMessageReceived")
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

        GlobalScope.launch(Dispatchers.Default) {

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
//endregion

//region Notification

    override suspend fun getRemoteContact() {
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
//            Timber.e(e)
        }
    }

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

    override fun notifyMessageReceived_NOTIF(messageId: String) {
        GlobalScope.launch {
            try {
                val messageReceivedReqDTO = MessageReceivedReqDTO(messageId)
                napoleonApi.notifyMessageReceived(messageReceivedReqDTO)
            } catch (e: Exception) {
//                    Timber.e(e)
            }
        }
    }

    override fun getIsOnCallPref() = Data.isOnCall

    override fun getContactSilenced(contactId: Int, silenced: (Boolean?) -> Unit) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                silenced(contactLocalDataSource.getContactSilenced(contactId))
            }
        }
    }

    override fun getContact(contactId: Int): ContactEntity? {
        return contactLocalDataSource.getContactById(contactId)
    }

    override fun getNotificationChannelCreated(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_CHANNEL_CREATED)
    }

    override fun setNotificationChannelCreated() {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_CHANNEL_CREATED,
            Constants.ChannelCreated.TRUE.state
        )
    }

    override fun getNotificationMessageChannelId(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_NOTIFICATION_MESSAGE_CHANNEL_ID
        )
    }

    override fun setNotificationMessageChannelId(newId: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_NOTIFICATION_MESSAGE_CHANNEL_ID,
            newId
        )
    }

    override fun getCustomNotificationChannelId(contactId: Int): String? {
        val contact = contactLocalDataSource.getContactById(contactId)
        return contact?.notificationId
    }

    override fun setCustomNotificationChannelId(contactId: Int, newId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            contactLocalDataSource.updateChannelId(contactId, newId)
        }
    }

    override fun getContactById(contactId: Int): ContactEntity? {
        return contactLocalDataSource.getContactById(contactId)
    }

    override fun updateStateChannel(contactId: Int, state: Boolean) {
        GlobalScope.launch(Dispatchers.IO) {
            contactLocalDataSource.updateStateChannel(contactId, state)
        }
    }

    override suspend fun insertQuote_NOTIF(quoteWebId: String, messageId: Int) {
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

//endregion

}