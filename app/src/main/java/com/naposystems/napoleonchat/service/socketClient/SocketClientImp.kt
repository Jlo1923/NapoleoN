package com.naposystems.napoleonchat.service.socketClient

import android.content.Context
import android.content.Intent
import android.util.Log
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.model.extractIdsAttachments
import com.naposystems.napoleonchat.model.extractIdsMessages
import com.naposystems.napoleonchat.model.toMessagesReqDTO
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.notificationClient.HandlerNotificationImp
import com.naposystems.napoleonchat.service.syncManager.SyncManager
import com.naposystems.napoleonchat.source.local.datasource.attachment.AttachmentLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.message.MessageLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.quoteMessage.QuoteLocalDataSource
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.QuoteEntity
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.contacts.ContactResDTO
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesReadedRESDTO
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesReceivedRESDTO
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesReqDTO
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesResDTO
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageDataEventRes
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageEventAttachmentRes
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageEventMessageRes
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageEventRes
import com.naposystems.napoleonchat.source.remote.dto.validateMessageEvent.ValidateMessage
import com.naposystems.napoleonchat.source.remote.dto.validateMessageEvent.ValidateMessageEventDTO
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Constants.AttachmentStatus
import com.naposystems.napoleonchat.utility.Constants.AttachmentStatus.READED
import com.naposystems.napoleonchat.utility.Constants.MessageStatus.UNREAD
import com.naposystems.napoleonchat.utility.Constants.SocketChannelStatus.SOCKECT_CHANNEL_STATUS_CONNECTED
import com.naposystems.napoleonchat.utility.Constants.StatusMustBe.RECEIVED
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.adapters.toIceCandidate
import com.naposystems.napoleonchat.utility.adapters.toSessionDescription
import com.naposystems.napoleonchat.utility.isNoCall
import com.naposystems.napoleonchat.webRTC.service.WebRTCService
import com.pusher.client.Pusher
import com.pusher.client.channel.*
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionState.CONNECTED
import com.pusher.client.connection.ConnectionStateChange
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.webrtc.SessionDescription
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class SocketClientImp
@Inject constructor(
    private val context: Context,
    private val pusher: Pusher,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val syncManager: SyncManager,
    private val napoleonApi: NapoleonApi,
    private val cryptoMessage: CryptoMessage,
    private val messageLocalDataSource: MessageLocalDataSource,
    private val attachmentLocalDataSource: AttachmentLocalDataSource,
    private val contactLocalDataSource: ContactLocalDataSource,
    private val quoteLocalDataSource: QuoteLocalDataSource
) : SocketClient, GetMessagesSocketListener {

    private val moshi: Moshi by lazy { Moshi.Builder().build() }
    private var userId: Int = Constants.UserNotExist.USER_NO_EXIST.user
    private lateinit var privateGeneralChannelName: String
    private lateinit var eventsFromSocketClientListener: EventsFromSocketClientListener

    // TODO: move that tan pronto como sea posible
    private val queueNewMessageDataEventRes = LinkedList<NewMessageDataEventRes>()

    companion object {
        const val HANGUP_CALL = 2
        const val CONTACT_WANT_CHANGE_TO_VIDEO = 3
        const val CONTACT_ACCEPT_CHANGE_TO_VIDEO = 4
        const val CONTACT_TURN_OFF_CAMERA = 5
        const val CONTACT_TURN_ON_CAMERA = 6
        const val CONTACT_CANCEL_CHANGE_TO_VIDEO = 7
        const val CONTACT_CANT_CHANGE_TO_VIDEO = 8
        const val TYPE = "type"
        const val ICE_CANDIDATE = "candidate"
        const val OFFER = "offer"
        const val ANSWER = "answer"
    }

    //region Conexion
    override fun setEventsFromSocketClientListener(eventsFromSocketClientListener: EventsFromSocketClientListener) {
        this.eventsFromSocketClientListener = eventsFromSocketClientListener
    }

    override fun getStatusSocket(): ConnectionState {
        return pusher.connection.state
    }

    override fun getStatusGlobalChannel(): Boolean {
        return if (pusher.getPrivateChannel(Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName) != null)
            if (pusher.getPrivateChannel(Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName).isSubscribed)
                pusher.getPrivateChannel(Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName).isSubscribed
            else
                Constants.SocketChannelStatus.SOCKECT_CHANNEL_STATUS_NOT_CONNECTED.status
        else
            Constants.SocketChannelStatus.SOCKECT_CHANNEL_STATUS_NOT_CONNECTED.status
    }

    override fun connectSocket() {

        Timber.d("LLAMADA PASO 4: EN CONNECT SOCKET")

        syncManager.setGetMessagesSocketListener(this)

        userId = syncManager.getUserId()

        if (userId != Constants.UserNotExist.USER_NO_EXIST.user) {

            privateGeneralChannelName =
                Constants.SocketChannelName.PRIVATE_GENERAL_CHANNEL_NAME.channelName + userId

            if (pusher.connection.state == ConnectionState.DISCONNECTED ||
                pusher.connection.state == ConnectionState.DISCONNECTING
            ) {

                Timber.d("LLAMADA PASO 4: CONNECT SOCKET")

                pusher.connect(object : ConnectionEventListener {

                    override fun onConnectionStateChange(connectionStateChange: ConnectionStateChange?) {

                        when (connectionStateChange?.currentState) {

                            CONNECTED -> {
                                Timber.d("LLAMADA PASO 4: SE CONECTO AL SOCKET")
                                handlerStateConnectedSocket()
                            }

                            ConnectionState.DISCONNECTING,
                            ConnectionState.DISCONNECTED,
                            ConnectionState.CONNECTING,
                            ConnectionState.RECONNECTING,
                            ConnectionState.ALL -> {
                                Timber.e("ConnectionStateChange Unhandling ${connectionStateChange.currentState}")
                            }
                        }
                    }

                    override fun onError(
                        message: String?,
                        code: String?,
                        e: java.lang.Exception?
                    ) {
                        Timber.d("LLAMADA PASO: CONECTAR A SOCKET onError message: $message, code: $code, e: ${e?.localizedMessage}")
//                        pusher.connect()
                    }
                })

            } else if (pusher.connection.state == CONNECTED) {
                Timber.d("LLAMADA PASO 4: PREVIAMENTE CONECTADA")
                handlerStateConnectedSocket()

            }
        }
    }

    override fun subscribeToPresenceChannel() {

        Timber.d("LLAMADA PASO 5: SUSCRIBIRSE AL CANAL DE LLAMADAS")

        NapoleonApplication.callModel?.let { callModel ->

            if (pusher.getPresenceChannel(callModel.channelName) == null) {

                Timber.d("LLAMADA PASO 5: CANAL PREVIO NO EXISTENTE")

                pusher.subscribePresence(
                    callModel.channelName,
                    object : PresenceChannelEventListener {
                        override fun onEvent(event: PusherEvent) = Unit

                        override fun onAuthenticationFailure(
                            message: String,
                            e: java.lang.Exception
                        ) = Unit

                        override fun onSubscriptionSucceeded(channelName: String) {

                            Timber.d("LLAMADA PASO 2: SUSCRIPCION LLAMADAS SUCCESS")

                            listenCallEvents(channelName)

                            Timber.d("LLAMADA PASO 2: ${NapoleonApplication.statusCall}")

                            if (NapoleonApplication.statusCall.isNoCall()) {

                                Timber.d("LLAMADA PASO 2: NO Esta en llamada")

//                            NapoleonApplication.statusCall = StatusCallEnum.STATUS_PROCESSING_CALL

                                when (callModel.typeCall) {

                                    Constants.TypeCall.IS_INCOMING_CALL -> {

                                        Timber.d("LLAMADA PASO 2: Llamada entrante")

                                        if (pusher.getPresenceChannel(callModel.channelName).users.size > 1) {

                                            pusher.getPresenceChannel(callModel.channelName).users.forEach {
                                                Timber.d("LLAMADA PASO User: ${it.id} ${it.info}")
                                            }

                                            Timber.d("LLAMADA PASO 3: Usuarios  mas de uno")

                                            eventsFromSocketClientListener.itsSubscribedToPresenceChannelIncomingCall()
                                        }
                                    }

                                    Constants.TypeCall.IS_OUTGOING_CALL -> {
                                        Timber.d("LLAMADA PASO 2: Llamada saliente")
                                        eventsFromSocketClientListener.itsSubscribedToPresenceChannelOutgoingCall()
                                    }
                                }
                            }
                        }

                        override fun onUsersInformationReceived(
                            channelName: String?,
                            users: MutableSet<User>?
                        ) = Unit

                        override fun userSubscribed(channelName: String?, user: User?) = Unit

                        override fun userUnsubscribed(channelName: String?, user: User?) = Unit
                    }
                )

            } else {

                Timber.d("LLAMADA PASO 5: CANAL PREVIO EXISTENTE")

                unSubscribePresenceChannel()

                subscribeToPresenceChannel()

            }


        }

    }

    override fun disconnectSocket() {

        Timber.e("LLAMADA PASO: SOCKET DISCONNECT")

        try {

            NapoleonApplication.callModel?.channelName?.let { channelName ->

                Timber.e("UNSUBSCRIBE PRESENCE")

                if (pusher.getPresenceChannel(channelName) != null) {

                    Timber.e("UNBIND PRESENCE")

                    pusher.getPresenceChannel(channelName)
                        .unbind(Constants.SocketEmitTriggers.CLIENT_CALL.trigger,
                            SubscriptionEventListener {}
                        )

                    //Unsubscribe Channels

                    pusher.unsubscribe(channelName)

                }
            }

            Timber.e("UNSUBSCRIBE GLOBAL")

            if (pusher.getPrivateChannel(Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName) != null) {

                Timber.e("UNBIND GLOBAL")

                pusher.getPrivateChannel(Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName)
                    .unbind(Constants.SocketEmitTriggers.CLIENT_CONVERSATION.trigger,
                        SubscriptionEventListener {}
                    )

                //Unsubscribe Channels

                pusher.unsubscribe(
                    Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName
                )

            }

            Timber.e("UNSUBSCRIBE GENERAL")

            if (pusher.getPrivateChannel(privateGeneralChannelName) != null) {

                Timber.e("UNBIND GENERAL")

                pusher.getPrivateChannel(privateGeneralChannelName)
                    .unbind(Constants.SocketListenEvents.DISCONNECT.event,
                        SubscriptionEventListener {}
                    )

                pusher.getPrivateChannel(privateGeneralChannelName)
                    .unbind(Constants.SocketListenEvents.NEW_MESSAGE.event,
                        SubscriptionEventListener {}
                    )

                pusher.getPrivateChannel(privateGeneralChannelName)
                    .unbind(Constants.SocketListenEvents.NOTIFY_MESSAGES_RECEIVED.event,
                        SubscriptionEventListener {}
                    )

                pusher.getPrivateChannel(privateGeneralChannelName)
                    .unbind(Constants.SocketListenEvents.NOTIFY_MESSAGE_READED.event,
                        SubscriptionEventListener {}
                    )

                pusher.getPrivateChannel(privateGeneralChannelName)
                    .unbind(Constants.SocketListenEvents.SEND_MESSAGES_DESTROY.event,
                        SubscriptionEventListener {}
                    )

                pusher.getPrivateChannel(privateGeneralChannelName)
                    .unbind(Constants.SocketListenEvents.CANCEL_OR_REJECT_FRIENDSHIP_REQUEST.event,
                        SubscriptionEventListener {}
                    )

                pusher.getPrivateChannel(privateGeneralChannelName)
                    .unbind(Constants.SocketListenEvents.BLOCK_OR_DELETE_FRIENDSHIP.event,
                        SubscriptionEventListener {}
                    )

                //Unsubscribe Channels

                pusher.unsubscribe(privateGeneralChannelName)

            }

            //Disconnect Pusher

            try {

                Timber.d("LLAMADA PASO: PUSHER.DISCONNECT")
                pusher.disconnect()

            } catch (e: Exception) {
                Timber.e("LLAMADA PASO: INTENTANDO DESCONECTAR PUSHER")
            }

        } catch (e: Exception) {
            Timber.e("Pusher Paso IN 7.3: $e")
        }
    }

    override fun unSubscribePresenceChannel() {

        Timber.e("LLAMADA PASO: INTENTANDO DESSUBSCRIBIR PRESENCIA ")

        NapoleonApplication.callModel?.channelName?.let { channelName ->

            Timber.d("LLAMADA PASO: DESUSCRIBIR A CANAL CHANNELNAME $channelName")

            try {
                if (pusher.getPresenceChannel(channelName) != null)
                    pusher.unsubscribe(channelName)
            } catch (e: Exception) {
                Timber.e(e.localizedMessage)
            }
        }
    }

    //TODO: Fusionar estos metodos
    override fun emitClientConversation(messages: List<ValidateMessage>) {

        Timber.d("Pusher 6.1: Emitir")

        try {

            val validateMessage = ValidateMessageEventDTO(messages)

            val adapterValidate = moshi.adapter(ValidateMessageEventDTO::class.java)

            val jsonObject = adapterValidate.toJson(validateMessage)

            if (jsonObject.isNotEmpty()) {

                pusher.getPrivateChannel(Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName)
                    .trigger(
                        Constants.SocketEmitTriggers.CLIENT_CONVERSATION.trigger,
                        jsonObject
                    )
            }


        } catch (e: Exception) {

            Timber.e("Pusher Paso IN 6.4: $e}")
        }

    }

    override fun emitClientConversation(messages: MessagesReqDTO) {

        Timber.d("Pusher 6.1: Emitir")

        try {
            val adapterValidate = moshi.adapter(MessagesReqDTO::class.java)

            val jsonObject = adapterValidate.toJson(messages)

            if (jsonObject.isNotEmpty()) {

                pusher.getPrivateChannel(Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName)
                    .trigger(
                        Constants.SocketEmitTriggers.CLIENT_CONVERSATION.trigger,
                        jsonObject
                    )
            }
        } catch (e: Exception) {
            Timber.e("Pusher Paso IN 6.4: $e}")
        }

    }

    override fun emitClientCall(jsonObject: JSONObject) {

        Timber.d("LLAMADA PASO: eventType: $jsonObject")
        try {
            NapoleonApplication.callModel?.channelName?.let { channelName ->
                if (pusher.getPresenceChannel(channelName) != null)
                    pusher.getPresenceChannel(channelName)
                        .trigger(
                            Constants.SocketEmitTriggers.CLIENT_CALL.trigger,
                            jsonObject.toString()
                        )
            }
        } catch (e: Exception) {
            Timber.e(e.localizedMessage)
        }

    }

    override fun emitClientCall(eventType: Int) {
        Timber.d("LLAMADA PASO: eventType: $eventType")
        try {
            NapoleonApplication.callModel?.channelName?.let { channelName ->
                if (pusher.getPresenceChannel(channelName) != null)
                    pusher.getPresenceChannel(channelName)
                        .trigger(
                            Constants.SocketEmitTriggers.CLIENT_CALL.trigger,
                            eventType.toString()
                        )
            }
        } catch (e: Exception) {
            Timber.e(e.localizedMessage)
        }
    }

    override fun isConnected(): Boolean =
        getStatusSocket() == CONNECTED && getStatusGlobalChannel() == SOCKECT_CHANNEL_STATUS_CONNECTED.status

//endregion

    // region Region Escuchadores de Eventos
    private fun subscribeChannels() {

        try {
            sharedPreferencesManager.putString(
                Constants.SharedPreferences.PREF_SOCKET_ID,
                pusher.connection.socketId
            )

            subscribeToPrivateGeneralChannel()

            subscribeToPrivateGlobalChannel()

        } catch (e: Exception) {

            Timber.e(e)
        }
    }

    private fun subscribeToPrivateGeneralChannel() {

        try {

            if (pusher.getPrivateChannel(privateGeneralChannelName) == null) {

                pusher.subscribePrivate(
                    privateGeneralChannelName,
                    object : PrivateChannelEventListener {

                        override fun onEvent(event: PusherEvent) = Unit

                        override fun onAuthenticationFailure(
                            message: String?,
                            e: java.lang.Exception?
                        ) = Unit

                        override fun onSubscriptionSucceeded(channelName: String?) {

                            //Metodos Generales
                            listenDisconnect()

                            //Metodos de mensajes
                            listenNewMessage()

                            listenNotifyMessagesReceived()

                            listenNotifyMessagesRead()

                            listenSendMessagesDestroy()

                            //Metodos de Contactos
                            listenCancelOrRejectFriendshipRequest()

                            listenBLockOrDeleteFriendship()

                            //Metodos de Llamadas
                            listenRejectedCall()

                            listenCancelCall()

                            syncManager.getMyMessages(null)

                            syncManager.verifyMessagesReceived()

                            syncManager.verifyMessagesRead()

                        }

                    })
            }

        } catch (e: Exception) {
            Timber.e("LLAMADA PASO IN 4.3:  subscribeToPrivateGlobalChannel: Exception: $e")
        }
    }

    private fun subscribeToPrivateGlobalChannel() {

        try {

            if (pusher.getPrivateChannel(Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName) == null) {

                pusher.subscribePrivate(
                    Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName,
                    object : PrivateChannelEventListener {
                        override fun onEvent(event: PusherEvent?) = Unit

                        override fun onAuthenticationFailure(
                            message: String?,
                            e: java.lang.Exception?
                        ) = Unit

                        override fun onSubscriptionSucceeded(channelName: String?) {
                            listenValidateConversationEvent()
                            if (NapoleonApplication.isVisible.not())
                                RxBus.publish(RxEvent.CreateNotification())
                        }
                    }
                )
            }

        } catch (e: Exception) {
            Timber.e("LLAMADA PASO IN 5.4:  subscribeToPrivateGlobalChannel: Exception: $e")
        }
    }

    private fun handlerStateConnectedSocket() {

        Timber.d("LLAMADA PASO 5: EN CONNECT SOCKET YA CONECTADO")

        subscribeChannels()

        NapoleonApplication.callModel?.let {
            if (it.mustSubscribeToPresenceChannel && it.channelName != "") {
                Timber.d("LLAMADA PASO 5: SE VA A SUSCRIBIR AL CANAL DE PRESENCIA")
                subscribeToPresenceChannel()
            }
        }

    }

    private fun listenDisconnect() {

        pusher.getPrivateChannel(privateGeneralChannelName)
            .bind(Constants.SocketListenEvents.DISCONNECT.event,
                object : PrivateChannelEventListener {
                    override fun onEvent(event: PusherEvent?) {
                        Timber.d("Socket disconnect ${event?.data}")
                        Timber.e("Pusher Paso IN 8.1: Desconectado")
                        pusher.connect()
                    }

                    override fun onAuthenticationFailure(
                        message: String?,
                        e: java.lang.Exception?
                    ) = Unit

                    override fun onSubscriptionSucceeded(channelName: String?) = Unit
                })
    }

    private fun listenNewMessage() {

        //TODO: modificar el metodo para validacion en DEV y PROD ya que en este momento no es posible
        // la validacion de ningun ambiente sin cifrar lo que dificultad la validacion de los metodos desde el servidor

        pusher.getPrivateChannel(privateGeneralChannelName)
            .bind(Constants.SocketListenEvents.NEW_MESSAGE.event,
                object : PrivateChannelEventListener {
                    override fun onEvent(event: PusherEvent?) {
                        Timber.d("Pusher: listenNewMessage:${event?.data}")
                        if (NapoleonApplication.isVisible) {
                            handleEventData(event)
                            Timber.d("Pusher: appVisible")
                        }
                    }

                    override fun onAuthenticationFailure(
                        message: String?,
                        e: java.lang.Exception?
                    ) = Unit

                    override fun onSubscriptionSucceeded(channelName: String?) = Unit
                })
    }

    private fun handleEventData(event: PusherEvent?) {
        try {

            //TODO: Refactorizar este metodo para que pueda ser utilizado tanto por SocketClient como
            // por HandlerNotificationMessageImp
            event?.data?.let { dataEventRes ->

                val jsonAdapterData: JsonAdapter<NewMessageEventRes> =
                    moshi.adapter(NewMessageEventRes::class.java)

                val dataEvent = jsonAdapterData.fromJson(dataEventRes)

                dataEvent?.data?.let { newMessageDataEventRes ->

                    Timber.d("syncManager.insertNewMessage")
                    /**
                     * Ojo con esto
                     */
                    if (queueNewMessageDataEventRes.isEmpty()) {
                        queueNewMessageDataEventRes.add(newMessageDataEventRes)
                        tryHandleNextItemInQueue()
                    } else {
                        queueNewMessageDataEventRes.add(newMessageDataEventRes)
                    }
                    //syncManager.insertNewMessage(newMessageDataEventRes)

                    val messageString: String = if (BuildConfig.ENCRYPT_API) {
                        cryptoMessage.decryptMessageBody(newMessageDataEventRes.message)
                    } else {
                        newMessageDataEventRes.message
                    }

                    val jsonAdapterMessage =
                        moshi.adapter(NewMessageEventMessageRes::class.java)

                    jsonAdapterMessage.fromJson(messageString)?.let { messageModel ->
                        if (mustEmitClientConversationByContactId(messageModel)) {
                            val listMessagesToReceived =
                                listOf(messageModel).toMessagesReqDTO(RECEIVED)
                            syncManager.notifyMessageReceived(listMessagesToReceived)
                            emitClientConversation(listMessagesToReceived)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
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

    @Synchronized
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

            val listMessagesToReceived =
                listOf(newMessageEventMessageRes).toMessagesReqDTO(RECEIVED)

            syncManager.notifyMessageReceived(listMessagesToReceived)
            emitClientConversation(listMessagesToReceived)

            //TODO: JuankDev12 tambien hay que emitir por sokect aqui solo esta emitiendo por notificacion
            // en el SocketClientImp se hace la emisión por tanto este proceso deberia hacerse allá

            RxBus.publish(RxEvent.NewMessageEventForCounter(newMessageDataEventRes.contactId))
        }
    }

    @Synchronized
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

            val listMessagesToReceived =
                listOf(newMessageEventMessageRes).toMessagesReqDTO(RECEIVED)

            syncManager.notifyMessageReceived(listMessagesToReceived)
            emitClientConversation(listMessagesToReceived)

            //TODO: JuankDev12 tambien hay que emitir por sokect aqui solo esta emitiendo por notificacion
            // en el SocketClientImp se hace la emisión por tanto este proceso deberia hacerse allá

            RxBus.publish(RxEvent.NewMessageEventForCounter(newMessageDataEventRes.contactId))
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

    private fun mustEmitClientConversationByContactId(messageModel: NewMessageEventMessageRes) =
        messageModel.numberAttachments == 0 &&
                NapoleonApplication.currentConversationContactId != messageModel.userAddressee

    private fun listenNotifyMessagesReceived() {

        pusher.getPrivateChannel(privateGeneralChannelName)
            .bind(Constants.SocketListenEvents.NOTIFY_MESSAGES_RECEIVED.event,

                object : PrivateChannelEventListener {
                    override fun onEvent(event: PusherEvent?) {
                        Timber.d("NotifyMessagesReceived: ${event?.data}")
                        event?.data?.let { messagesReceivedResDto ->
                            handleEventMessageReceivedData(messagesReceivedResDto)
                        }
                    }

                    override fun onAuthenticationFailure(
                        message: String?,
                        e: java.lang.Exception?
                    ) = Unit

                    override fun onSubscriptionSucceeded(
                        channelName: String?
                    ) = Unit

                })
    }

    private fun handleEventMessageReceivedData(messagesReceivedResDto: String) {

        val jsonAdapter: JsonAdapter<MessagesReceivedRESDTO> =
            moshi.adapter(MessagesReceivedRESDTO::class.java)
        val dataDataEvent = jsonAdapter.fromJson(messagesReceivedResDto)

        dataDataEvent?.data?.let { messagesResDTO ->

            val listIdMsgs = messagesResDTO.extractIdsMessages()
            if (listIdMsgs.isEmpty().not()) {
                syncManager.updateMessagesStatus(
                    listIdMsgs,
                    UNREAD.status
                )
            }

            val idsAttachments = messagesResDTO.extractIdsAttachments()
            if (idsAttachments.isNotEmpty()) {
                val ids = idsAttachments.filter { syncManager.existAttachmentById(it) }
                if (ids.isNotEmpty()) {
                    syncManager.updateAttachmentsStatus(ids, AttachmentStatus.RECEIVED.status)
                }

                /**
                 * Debemos consultar cuantos attachments tiene marcados como recibidos, si tiene todos
                 * como recibidos, marcamos el mensaje como recibido y update selfdestruction time
                 */
                if (ids.isNotEmpty()) {
                    syncManager.tryMarkMessageParentAsReceived(ids)
                }
            }


        }
    }

    private fun listenNotifyMessagesRead() {
        pusher.getPrivateChannel(privateGeneralChannelName)
            .bind(Constants.SocketListenEvents.NOTIFY_MESSAGE_READED.event,

                object : PrivateChannelEventListener {

                    override fun onEvent(event: PusherEvent?) {
                        Timber.d("NotifyMessageReaded: ${event?.data}")
                        event?.data?.let {
                            handleEventMessagesReadData(it)
                        }
                        syncManager.verifyMessagesRead()
                    }

                    override fun onAuthenticationFailure(
                        message: String?,
                        e: java.lang.Exception?
                    ) = Unit

                    override fun onSubscriptionSucceeded(channelName: String?) = Unit
                })
    }

    private fun handleEventMessagesReadData(it: String) {

        val jsonAdapter: JsonAdapter<MessagesReadedRESDTO> =
            moshi.adapter(MessagesReadedRESDTO::class.java)
        val dataEvent = jsonAdapter.fromJson(it)

        dataEvent?.let { messagesReadedDTO ->

            val listIdMsgs = messagesReadedDTO.data.extractIdsMessages()
            if (listIdMsgs.isEmpty().not()) {
                syncManager.updateMessagesStatus(
                    listIdMsgs,
                    UNREAD.status
                )
            }

            val idsAttachments = messagesReadedDTO.data.extractIdsAttachments()
            if (idsAttachments.isNotEmpty()) {
                val ids = idsAttachments.filter { syncManager.existAttachmentById(it) }
                if (ids.isNotEmpty()) {
                    syncManager.updateAttachmentsStatus(ids, READED.status)
                }

                /**
                 * Debemos consultar cuantos attachments tiene marcados como recibidos, si tiene todos
                 * como recibidos, marcamos el mensaje como recibido y update selfdestruction time
                 */
                if (ids.isNotEmpty()) {
                    syncManager.tryMarkMessageParentAsRead(ids)
                }
            }

        }
    }

    private fun listenSendMessagesDestroy() {

        pusher.getPrivateChannel(privateGeneralChannelName)
            .bind(Constants.SocketListenEvents.SEND_MESSAGES_DESTROY.event,
                object : PrivateChannelEventListener {
                    override fun onEvent(event: PusherEvent?) {
                        Timber.d("SendMessagesDestroyEvent: ${event?.data}")
                        syncManager.getDeletedMessages()
                    }

                    override fun onAuthenticationFailure(
                        message: String?,
                        e: java.lang.Exception?
                    ) = Unit

                    override fun onSubscriptionSucceeded(channelName: String?) = Unit
                })
    }

    private fun listenValidateConversationEvent() {

        pusher.getPrivateChannel(Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName)
            .bind(Constants.SocketEmitTriggers.CLIENT_CONVERSATION.trigger,
                object : PrivateChannelEventListener {
                    override fun onEvent(event: PusherEvent?) {
                        try {
                            event?.data?.let { messagesResDTO ->

                                val jsonAdapter: JsonAdapter<MessagesResDTO> =
                                    moshi.adapter(MessagesResDTO::class.java)

                                //TODO: Pasar esto a una funcion ya que la estructura se repite
                                val dataEvent = jsonAdapter.fromJson(messagesResDTO)

                                //filtra los MESSAGES
                                val messages = dataEvent?.messages?.filter {
                                    it.user == userId
                                }?.filter {
                                    syncManager.existMessageById(it.id)
                                }

                                //Seccion Actualizar MESSAGE UNREAD
                                messages?.filter {
                                    it.status == Constants.MessageEventType.UNREAD.status &&
                                            it.type == Constants.MessageType.TEXT.type
                                }?.map {
                                    it.id
                                }?.let {
                                    if (it.isNotEmpty()) {
                                        syncManager.updateMessagesStatus(
                                            it,
                                            UNREAD.status
                                        )
                                    }
                                }

                                //Seccion Actualizar MESSAGE READED
                                messages?.filter {
                                    it.status == Constants.MessageEventType.READ.status &&
                                            it.type == Constants.MessageType.TEXT.type
                                }?.map {
                                    it.id
                                }?.let {
                                    if (it.isNotEmpty()) {
                                        syncManager.updateMessagesStatus(
                                            it,
                                            Constants.MessageStatus.READED.status
                                        )
                                    }
                                }

                                //filtra los ATTACHMENTS
                                val attachments = dataEvent?.messages?.filter {
                                    it.user == userId
                                }?.filter {
                                    syncManager.existAttachmentById(it.id)
                                }

                                //Seccion Actualizar ATTACHMENT UNREAD
                                attachments?.filter {
                                    it.status == Constants.MessageEventType.UNREAD.status &&
                                            it.type == Constants.MessageType.ATTACHMENT.type
                                }?.map {
                                    it.id
                                }?.let {
                                    syncManager.updateAttachmentsStatus(
                                        it,
                                        AttachmentStatus.RECEIVED.status
                                    )
                                }

                                //Seccion Actualizar ATTACHMENT READED
                                attachments?.filter {
                                    it.status == Constants.MessageEventType.READ.status &&
                                            it.type == Constants.MessageType.ATTACHMENT.type
                                }?.map {
                                    it.id
                                }?.let {
                                    syncManager.validateMessageType(
                                        it,
                                        READED.status
                                    )
                                }

                            }
                        } catch (e: Exception) {
                            Timber.e(e)
                        }
                    }

                    override fun onSubscriptionSucceeded(channelName: String?) = Unit

                    override fun onAuthenticationFailure(
                        message: String?,
                        e: java.lang.Exception?
                    ) = Unit
                }
            )
    }

    private fun listenCancelOrRejectFriendshipRequest() {

        pusher.getPrivateChannel(privateGeneralChannelName)
            .bind(Constants.SocketListenEvents.CANCEL_OR_REJECT_FRIENDSHIP_REQUEST.event,
                object : PrivateChannelEventListener {
                    override fun onEvent(event: PusherEvent?) {
                        RxBus.publish(RxEvent.CancelOrRejectFriendshipRequestEvent())
                    }

                    override fun onSubscriptionSucceeded(channelName: String?) = Unit

                    override fun onAuthenticationFailure(
                        message: String?,
                        e: java.lang.Exception?
                    ) = Unit
                }
            )
    }

    private fun listenBLockOrDeleteFriendship() {

        pusher.getPrivateChannel(privateGeneralChannelName)
            .bind(Constants.SocketListenEvents.BLOCK_OR_DELETE_FRIENDSHIP.event,
                object : PrivateChannelEventListener {
                    override fun onEvent(event: PusherEvent) {
                        Timber.d("-- BlockOrDeleteFrienshipEvent ${event.data}")
                        val jsonObject = JSONObject(event.data)
                        if (jsonObject.has("data")) {
                            jsonObject.getJSONObject("data").let { jsonData ->
                                if (jsonData.has("contact_id")) {
                                    jsonData.getInt("contact_id").let { contactId ->
                                        syncManager.deleteContact(contactId)
                                        RxBus.publish(
                                            RxEvent.ContactBlockOrDelete(
                                                jsonData.getInt("contact_id")
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    override fun onAuthenticationFailure(
                        message: String?,
                        e: java.lang.Exception?
                    ) = Unit

                    override fun onSubscriptionSucceeded(channelName: String?) = Unit

                })
    }

    private fun listenRejectedCall() {

        pusher.getPrivateChannel(privateGeneralChannelName)
            .bind(Constants.SocketListenEvents.REJECTED_CALL.event,
                object : PrivateChannelEventListener {
                    override fun onEvent(event: PusherEvent) {

                        try {

                            Timber.d("RejectedCallEvent: ${event.data}, notificationId: ${HandlerNotificationImp.NOTIFICATION_CALL_ACTIVE}")

                            val jsonObject = JSONObject(event.data)

                            if (jsonObject.has("data")) {

                                val jsonData = jsonObject.getJSONObject("data")

                                if (jsonData.has("channel_private")) {

                                    val presenceChannel = jsonData.getString("channel_private")

                                    Timber.d("LLAMADA PASO: RECHAZAR LLAMADA")

                                    NapoleonApplication.callModel?.let { callModel ->
                                        if (callModel.channelName == presenceChannel) {
                                            if (NapoleonApplication.isShowingCallActivity) {
                                                Timber.d("LLAMADA PASO: RECHAZAR LLAMADA ESTA MOSTRANDO LLAMADA")
                                                eventsFromSocketClientListener.contactRejectCall()
                                            } else {
                                                Timber.d("LLAMADA PASO: RECHAZAR LLAMADA NO ESTA MOSTRANDO LLAMADA")
                                                val intent =
                                                    Intent(context, WebRTCService::class.java)
                                                intent.action = WebRTCService.ACTION_DENY_CALL
                                                context.startService(intent)
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Timber.e(e)
                        }
                    }

                    override fun onAuthenticationFailure(
                        message: String?,
                        e: java.lang.Exception?
                    ) = Unit

                    override fun onSubscriptionSucceeded(channelName: String?) = Unit
                })
    }

    private fun listenCancelCall() {

        pusher.getPrivateChannel(privateGeneralChannelName)
            .bind(Constants.SocketListenEvents.CANCEL_CALL.event,
                object : PrivateChannelEventListener {
                    override fun onEvent(event: PusherEvent) {

                        try {

                            Timber.d("CancelCallEvent: ${event.data}, notificationId: ${HandlerNotificationImp.NOTIFICATION_CALL_ACTIVE}")

                            val jsonObject = JSONObject(event.data)

                            if (jsonObject.has("data")) {

                                val jsonData = jsonObject.getJSONObject("data")

                                if (jsonData.has("channel_private")) {

                                    val presenceChannel = jsonData.getString("channel_private")

                                    Timber.d("LLAMADA PASO: CANCELAR LLAMADA")

                                    NapoleonApplication.callModel?.let { callModel ->
                                        if (callModel.channelName == presenceChannel) {
                                            if (NapoleonApplication.isShowingCallActivity) {
                                                Timber.d("LLAMADA PASO: RECHAZAR LLAMADA ESTA MOSTRANDO LLAMADA")
                                                eventsFromSocketClientListener.contactCancelCall()
                                            } else {
                                                Timber.d("LLAMADA PASO: RECHAZAR LLAMADA NO ESTA MOSTRANDO LLAMADA")
                                                val intent =
                                                    Intent(context, WebRTCService::class.java)
                                                intent.action = WebRTCService.ACTION_CALL_END
                                                context.startService(intent)
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Timber.e(e)
                        }
                    }

                    override fun onAuthenticationFailure(
                        message: String?,
                        e: java.lang.Exception?
                    ) = Unit

                    override fun onSubscriptionSucceeded(channelName: String?) = Unit
                })
    }

    private fun listenCallEvents(channelName: String) {
        try {
            pusher.getPresenceChannel(channelName)
                .bind(
                    Constants.SocketEmitTriggers.CLIENT_CALL.trigger,
                    object : PresenceChannelEventListener {

                        override fun onEvent(event: PusherEvent) {
                            try {

                                val eventType = event.data.toIntOrNull()

                                if (eventType != null) {

                                    Timber.d("LLAMADA PASO: Llega el evento de llamada ${eventType}")

                                    when (eventType) {

                                        CONTACT_WANT_CHANGE_TO_VIDEO -> {
                                            Timber.d("LLAMADA PASO: CONTACT_WANT_CHANGE_TO_VIDEO")
                                            eventsFromSocketClientListener.contactWantChangeToVideoCall()
                                        }
                                        CONTACT_ACCEPT_CHANGE_TO_VIDEO -> {
                                            Timber.d("LLAMADA PASO: CONTACT_ACCEPT_CHANGE_TO_VIDEO")
                                            eventsFromSocketClientListener.contactAcceptChangeToVideoCall()
                                        }
                                        CONTACT_CANCEL_CHANGE_TO_VIDEO -> {
                                            Timber.d("LLAMADA PASO: CONTACT_CANCEL_CHANGE_TO_VIDEO")
                                            eventsFromSocketClientListener.contactCancelChangeToVideoCall()
                                        }
                                        CONTACT_CANT_CHANGE_TO_VIDEO -> {
                                            Timber.d("LLAMADA PASO: CONTACT_CANT_CHANGE_TO_VIDEO")
                                            eventsFromSocketClientListener.contactCantChangeToVideoCall()
                                        }
                                        CONTACT_TURN_ON_CAMERA -> {
                                            Timber.d("LLAMADA PASO: CONTACT_TURN_ON_CAMERA")
                                            eventsFromSocketClientListener.toggleContactCamera(
                                                isVisible = true
                                            )
                                        }

                                        CONTACT_TURN_OFF_CAMERA -> {
                                            Timber.d("LLAMADA PASO: CONTACT_TURN_OFF_CAMERA")
                                            eventsFromSocketClientListener.toggleContactCamera(
                                                isVisible = false
                                            )
                                        }

                                        HANGUP_CALL -> {
                                            Timber.d("LLAMADA PASO: HANGUP_CALL")
                                            eventsFromSocketClientListener.contactHasHangup()
                                        }
                                    }

                                } else {

                                    val jsonData = JSONObject(event.data)

                                    if (jsonData.has(TYPE)) {

                                        when (jsonData.getString(TYPE)) {

                                            ICE_CANDIDATE -> {
                                                eventsFromSocketClientListener.iceCandidateReceived(
                                                    jsonData.toIceCandidate()
                                                )
                                            }

                                            OFFER -> {
                                                eventsFromSocketClientListener.offerReceived(
                                                    jsonData.toSessionDescription(
                                                        SessionDescription.Type.OFFER
                                                    )
                                                )
                                            }

                                            ANSWER -> {
                                                eventsFromSocketClientListener.answerReceived(
                                                    jsonData.toSessionDescription(
                                                        SessionDescription.Type.ANSWER
                                                    )
                                                )
                                            }

                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Timber.e(e)
                            }
                        }

                        override fun onAuthenticationFailure(
                            message: String?,
                            e: java.lang.Exception?
                        ) = Unit

                        override fun onSubscriptionSucceeded(channelName: String?) = Unit

                        override fun onUsersInformationReceived(
                            channelName: String?,
                            users: MutableSet<User>?
                        ) = Unit

                        override fun userSubscribed(channelName: String?, user: User?) = Unit

                        override fun userUnsubscribed(channelName: String?, user: User?) = Unit
                    })
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
//endregion

    private fun availableToReceived(attachments: List<NewMessageEventAttachmentRes>): Boolean {

        val attachment: NewMessageEventAttachmentRes? = attachments.firstOrNull() {
            it.type == Constants.AttachmentType.IMAGE.type ||
                    it.type == Constants.AttachmentType.AUDIO.type ||
                    it.type == Constants.AttachmentType.VIDEO.type ||
                    it.type == Constants.AttachmentType.DOCUMENT.type
        }

        return attachment != null

    }

    override fun emitSocketClientConversation(listMessagesReceived: MessagesReqDTO) {
        emitClientConversation(listMessagesReceived)
    }
}