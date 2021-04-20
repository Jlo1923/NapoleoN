package com.naposystems.napoleonchat.service.socketClient

import android.content.Context
import android.content.Intent
import android.util.Log
import android.os.Bundle
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.model.CallModel
import com.naposystems.napoleonchat.model.conversationCall.IncomingCall
import com.naposystems.napoleonchat.model.extractIdsAttachments
import com.naposystems.napoleonchat.model.extractIdsMessages
import com.naposystems.napoleonchat.model.toMessagesReqDTO
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.notificationClient.HandlerNotificationImp
import com.naposystems.napoleonchat.service.syncManager.SyncManager
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesReadedRESDTO
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesReceivedRESDTO
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesReqDTO
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesResDTO
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageEventAttachmentRes
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageEventMessageRes
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageEventRes
import com.naposystems.napoleonchat.source.remote.dto.validateMessageEvent.ValidateMessage
import com.naposystems.napoleonchat.source.remote.dto.validateMessageEvent.ValidateMessageEventDTO
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.adapters.toIceCandidate
import com.naposystems.napoleonchat.utility.adapters.toSessionDescription
import com.naposystems.napoleonchat.webRTC.service.WebRTCService
import com.pusher.client.Pusher
import com.pusher.client.channel.*
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.json.JSONObject
import org.webrtc.SessionDescription
import timber.log.Timber
import javax.inject.Inject

class SocketClientImp
@Inject constructor(
    private val context: Context,
    private val pusher: Pusher,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val syncManager: SyncManager,
    private val cryptoMessage: CryptoMessage
) : SocketClient {

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    private var userId: Int = Constants.UserNotExist.USER_NO_EXIST.user

    private lateinit var privateGeneralChannelName: String

    private lateinit var socketEventListener: SocketEventListener

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
    override fun setSocketEventListener(socketEventListener: SocketEventListener) {
        this.socketEventListener = socketEventListener
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

    override fun connectSocket(mustSubscribeToPresenceChannel: Boolean, callModel: CallModel?) {

        Timber.d("LLAMADA PASO: EN CONNECT SOCKET mustSubscribeToPresenceChannel: $mustSubscribeToPresenceChannel")

        userId = syncManager.getUserId()

        if (userId != Constants.UserNotExist.USER_NO_EXIST.user) {

            privateGeneralChannelName =
                Constants.SocketChannelName.PRIVATE_GENERAL_CHANNEL_NAME.channelName + userId

            if (pusher.connection.state == ConnectionState.DISCONNECTED ||
                pusher.connection.state == ConnectionState.DISCONNECTING
            ) {

                Timber.d("LLAMADA PASO: CONECTAR A SOCKET SI ESTA DESCONECTADO mustSubscribeToPresenceChannel: $mustSubscribeToPresenceChannel")

                pusher.connect(object : ConnectionEventListener {

                    override fun onConnectionStateChange(change: ConnectionStateChange?) {

                        if (change?.currentState == ConnectionState.CONNECTED) {

                            subscribeChannels()

                            if (mustSubscribeToPresenceChannel) {
                                Timber.d("LLAMADA PASO: CONEXION SUCCESS")
                                callModel?.let { subscribeToPresenceChannel(it) }
                            }
                        }
                    }

                    override fun onError(message: String?, code: String?, e: java.lang.Exception?) {
                        Timber.d("LLAMADA PASO: CONECTAR A SOCKET onError message: $message, code: $code, e: ${e?.localizedMessage}")
                        pusher.connect()
                    }
                })

            } else if (pusher.connection.state == ConnectionState.CONNECTED) {

                Timber.d("LLAMADA PASO: EN SOCKET CONECTADO  mustSubscribeToPresenceChannel: $mustSubscribeToPresenceChannel")

                if (NapoleonApplication.isVisible)
                    subscribeChannels()

                if (mustSubscribeToPresenceChannel) {
                    Timber.d("LLAMADA PASO: EN SOCKET CONECTADO")
                    callModel?.let { subscribeToPresenceChannel(it) }
                }

            }
        }
    }

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
                            listenIncomingCall()

                            listenRejectedCall()

                            listenCancelCall()

                            syncManager.getMyMessages(null)

                            syncManager.verifyMessagesReceived()

                            syncManager.verifyMessagesRead()

                        }

                    })
            }

        } catch (e: Exception) {
            Timber.e("Pusher Paso IN 4.3:  subscribeToPrivateGlobalChannel: Exception: $e")
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
            Timber.e("Pusher Paso IN 5.4:  subscribeToPrivateGlobalChannel: Exception: $e")
        }
    }

    override fun subscribeToPresenceChannel(callModel: CallModel) {

        Timber.d("LLAMADA PASO 1: SUSCRIBIRSE AL CANAL DE LLAMADAS ${callModel.channelName}")

        if (pusher.getPresenceChannel(callModel.channelName) == null) {

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

                        if (pusher.getPresenceChannel(callModel.channelName).users.size > 1) {
                            Timber.d("LLAMADA PASO 3: Usuarios  mas de uno")
                            if (callModel.typeCall == Constants.TypeCall.IS_INCOMING_CALL)
                                socketEventListener.itsSubscribedToPresenceChannelIncomingCall(
                                    callModel
                                )

                        } else {
                            Timber.d("LLAMADA PASO 3: Usuarios solo uno")
                            if (callModel.typeCall == Constants.TypeCall.IS_OUTGOING_CALL)
                                socketEventListener.itsSubscribedToPresenceChannelOutgoingCall(
                                    callModel
                                )
                        }
                    }

                    override fun onUsersInformationReceived(
                        channelName: String?,
                        users: MutableSet<User>?
                    ) = Unit

                    override fun userSubscribed(channelName: String?, user: User?) = Unit

                    override fun userUnsubscribed(channelName: String?, user: User?) = Unit
                })

        } else {

            unSubscribePresenceChannel(channelName = callModel.channelName)

            subscribeToPresenceChannel(callModel)

        }

    }

    override fun disconnectSocket() {

        Timber.e("SOCKET DISCONNECT")

        try {

            if (pusher.connection.state == ConnectionState.CONNECTED ||
                pusher.connection.state == ConnectionState.CONNECTING
            ) {

                Timber.e("UNSUBSCRIBE GLOBAL")

                if (pusher.getPrivateChannel(Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName) != null) {

                    Timber.e("UNBIND GLOBAL")

                    pusher.getPrivateChannel(
                        Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName
                    )
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
                pusher.disconnect()

            }
        } catch (e: Exception) {
            Timber.e("Pusher Paso IN 7.3: $e")
        }
    }

    override fun unSubscribePresenceChannel(channelName: String) {
        if (pusher.getPresenceChannel(channelName) != null) {
            Timber.d("LLAMADA PASO: DESUSCRIBIR A CANAL CHANNELNAME $channelName")
            NapoleonApplication.isCurrentOnCall = false
            pusher.unsubscribe(channelName)
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

//    private fun emitClientConversation(messages: ValidateMessage) {
//        emitClientConversation(arrayListOf(messages))
//    }

    override fun emitClientCall(channel: String, jsonObject: JSONObject) {

        if (pusher.getPresenceChannel(channel) != null) {
            if (pusher.connection.state == ConnectionState.CONNECTED) {
                pusher.getPresenceChannel(channel)
                    .trigger(
                        Constants.SocketEmitTriggers.CLIENT_CALL.trigger,
                        jsonObject.toString()
                    )

                Timber.d("Emit to Call $jsonObject")
            }
        }
    }

    override fun emitClientCall(channel: String, eventType: Int) {

        Timber.d("LLAMADA PASO: channel $channel eventType: $eventType")

        if (pusher.getPresenceChannel(channel) != null)
            pusher.getPresenceChannel(channel)
                .trigger(
                    Constants.SocketEmitTriggers.CLIENT_CALL.trigger,
                    eventType.toString()
                )
    }
    //endregion

    // region Region Escuchadores de Eventos
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

                            Timber.d("Pusher: appVisible")

                            try {

                                //TODO: Refactorizar este metodo para que pueda ser utilizado tanto por SocketClient como
                                // por HandlerNotificationMessageImp
                                event?.data?.let { dataEventRes ->

                                    val jsonAdapterData: JsonAdapter<NewMessageEventRes> =
                                        moshi.adapter(NewMessageEventRes::class.java)

                                    val dataEvent = jsonAdapterData.fromJson(dataEventRes)

                                    dataEvent?.data?.let { newMessageDataEventRes ->

                                        Timber.d("syncManager.insertNewMessage")
                                        syncManager.insertNewMessage(newMessageDataEventRes)

                                        val messageString: String = if (BuildConfig.ENCRYPT_API) {
                                            cryptoMessage.decryptMessageBody(newMessageDataEventRes.message)
                                        } else {
                                            newMessageDataEventRes.message
                                        }

                                        val jsonAdapterMessage: JsonAdapter<NewMessageEventMessageRes> =
                                            moshi.adapter(NewMessageEventMessageRes::class.java)

                                        jsonAdapterMessage.fromJson(messageString)
                                            ?.let { messageModel ->

                                                if (messageModel.numberAttachments == 0 &&
                                                    NapoleonApplication.currentConversationContactId != messageModel.userAddressee
                                                ) {

                                                    val listMessagesToReceived = listOf(
                                                        messageModel
                                                    ).toMessagesReqDTO(Constants.StatusMustBe.RECEIVED)


                                                    syncManager.notifyMessageReceived(
                                                        listMessagesToReceived
                                                    )

                                                    emitClientConversation(listMessagesToReceived)

                                                }
                                            }
                                    }
                                }

                            } catch (e: Exception) {
                                Timber.e(e)
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

    private fun listenNotifyMessagesReceived() {

        pusher.getPrivateChannel(privateGeneralChannelName)
            .bind(Constants.SocketListenEvents.NOTIFY_MESSAGES_RECEIVED.event,
                object : PrivateChannelEventListener {
                    override fun onEvent(event: PusherEvent?) {

                        Timber.d("NotifyMessagesReceived: ${event?.data}")

                        event?.data?.let { messagesReceivedResDto ->

                            val jsonAdapter: JsonAdapter<MessagesReceivedRESDTO> =
                                moshi.adapter(MessagesReceivedRESDTO::class.java)

                            val dataDataEvent = jsonAdapter.fromJson(messagesReceivedResDto)

                            dataDataEvent?.data?.let { messagesResDTO ->

                                syncManager.updateMessagesStatus(
                                    messagesResDTO.extractIdsMessages(),
                                    Constants.MessageStatus.UNREAD.status
                                )

                                val idsAttachments = messagesResDTO.extractIdsAttachments()

                                if (idsAttachments.isNotEmpty()) {

                                    val ids = idsAttachments.filter {
                                        syncManager.existAttachmentById(it)
                                    }

                                    syncManager.updateAttachmentsStatus(
                                        ids,
                                        Constants.AttachmentStatus.DOWNLOADING.status
                                    )

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

    private fun listenNotifyMessagesRead() {
        pusher.getPrivateChannel(privateGeneralChannelName)
            .bind(Constants.SocketListenEvents.NOTIFY_MESSAGE_READED.event,
                object : PrivateChannelEventListener {
                    override fun onEvent(event: PusherEvent?) {
                        Timber.d("NotifyMessageReaded: ${event?.data}")

                        event?.data?.let {
                            val jsonAdapter: JsonAdapter<MessagesReadedRESDTO> =
                                moshi.adapter(MessagesReadedRESDTO::class.java)

                            val dataEvent = jsonAdapter.fromJson(it)

                            dataEvent?.let { messagesReadedDTO ->

                                syncManager.updateMessagesStatus(
                                    messagesReadedDTO.data.messages.map { it.id },
                                    Constants.MessageStatus.READED.status
                                )
                            }
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
                                            it.type == Constants.MessageTypeByStatus.MESSAGE.type
                                }?.map {
                                    it.id
                                }?.let {
                                    syncManager.updateMessagesStatus(
                                        it,
                                        Constants.MessageStatus.UNREAD.status
                                    )
                                }

                                //Seccion Actualizar MESSAGE READED
                                messages?.filter {
                                    it.status == Constants.MessageEventType.READ.status &&
                                            it.type == Constants.MessageTypeByStatus.MESSAGE.type
                                }?.map {
                                    it.id
                                }?.let {
                                    syncManager.updateMessagesStatus(
                                        it,
                                        Constants.MessageStatus.READED.status
                                    )
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
                                            it.type == Constants.MessageTypeByStatus.ATTACHMENT.type
                                }?.map {
                                    it.id
                                }?.let {
                                    syncManager.updateAttachmentsStatus(
                                        it,
                                        Constants.MessageStatus.UNREAD.status
                                    )
                                }

                                //Seccion Actualizar ATTACHMENT READED
                                attachments?.filter {
                                    it.status == Constants.MessageEventType.READ.status &&
                                            it.type == Constants.MessageTypeByStatus.ATTACHMENT.type
                                }?.map {
                                    it.id
                                }?.let {
                                    syncManager.validateMessageType(
                                        it,
                                        Constants.MessageStatus.READED.status
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

    private fun listenIncomingCall() {
        pusher.getPrivateChannel(privateGeneralChannelName)
            .bind(Constants.SocketListenEvents.CALL_FRIEND.event,
                object : PrivateChannelEventListener {
                    override fun onEvent(event: PusherEvent) {

                        Timber.d("LLAMADA PASO 1: LlAMADA ENTRANTE")

                        if (NapoleonApplication.isVisible) {

                            Timber.d("LLAMADA PASO 2: APLICACION VISIBLE")

                            try {

                                val adapter: JsonAdapter<IncomingCall> =
                                    moshi.adapter(IncomingCall::class.java)

                                adapter.fromJson(event.data)?.let { incomingCall ->

                                    val channel = "presence-${incomingCall.data.channel}"

                                    if (NapoleonApplication.isCurrentOnCall) {

                                        syncManager.rejectCall(
                                            incomingCall.data.contactId,
                                            channel
                                        )

                                    } else {

                                        Timber.d("LLAMADA PASO 3: USUARIO NO ESTA EN LLAMADA")

                                        NapoleonApplication.isCurrentOnCall = true

                                        subscribeToPresenceChannel(
                                            CallModel(
                                                contactId = incomingCall.data.contactId,
                                                channelName = channel,
                                                isVideoCall = incomingCall.data.isVideoCall,
                                                offer = incomingCall.data.offer,
                                                typeCall = Constants.TypeCall.IS_INCOMING_CALL,
                                                isFromClosedApp = Constants.FromClosedApp.NO
                                            )
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                Timber.e(e)
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

                            NapoleonApplication.isShowingCallActivity = false

                            Timber.d("RejectedCallEvent: ${event.data}, notificationId: ${HandlerNotificationImp.NOTIFICATION_CALL_ACTIVE}")

                            val jsonObject = JSONObject(event.data)

                            if (jsonObject.has("data")) {

                                val jsonData = jsonObject.getJSONObject("data")

                                if (jsonData.has("channel_private")) {

                                    val presenceChannel = jsonData.getString("channel_private")

                                    if (NapoleonApplication.isShowingCallActivity)
                                        socketEventListener.contactRejectCall(presenceChannel)
                                    else {
                                        val callModel = CallModel()
                                        callModel.channelName = presenceChannel
                                        val intent = Intent(context, WebRTCService::class.java)
                                        intent.action = WebRTCService.ACTION_CALL_END
                                        intent.putExtras(Bundle().apply {
                                            putSerializable(
                                                Constants.CallKeys.CALL_MODEL,
                                                callModel
                                            )
                                        })
                                        context.startService(intent)
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

                            NapoleonApplication.isShowingCallActivity = false

                            Timber.d("CancelCallEvent: ${event.data}, notificationId: ${HandlerNotificationImp.NOTIFICATION_CALL_ACTIVE}")

                            val jsonObject = JSONObject(event.data)

                            if (jsonObject.has("data")) {

                                val jsonData = jsonObject.getJSONObject("data")

                                if (jsonData.has("channel_private")) {

                                    val presenceChannel = jsonData.getString("channel_private")

                                    if (NapoleonApplication.isShowingCallActivity)
                                        socketEventListener.contactCancelCall(presenceChannel)
                                    else {
                                        val callModel = CallModel()
                                        callModel.channelName = presenceChannel
                                        val intent = Intent(context, WebRTCService::class.java)
                                        intent.action = WebRTCService.ACTION_CALL_END
                                        intent.putExtras(Bundle().apply {
                                            putSerializable(
                                                Constants.CallKeys.CALL_MODEL,
                                                callModel
                                            )
                                        })
                                        context.startService(intent)
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
                                            socketEventListener.contactWantChangeToVideoCall(
                                                event.channelName
                                            )
                                        }
                                        CONTACT_ACCEPT_CHANGE_TO_VIDEO -> {
                                            Timber.d("LLAMADA PASO: CONTACT_ACCEPT_CHANGE_TO_VIDEO")
                                            socketEventListener.contactAcceptChangeToVideoCall(
                                                event.channelName
                                            )
                                        }
                                        CONTACT_CANCEL_CHANGE_TO_VIDEO -> {
                                            Timber.d("LLAMADA PASO: CONTACT_CANCEL_CHANGE_TO_VIDEO")
                                            socketEventListener.contactCancelChangeToVideoCall(
                                                event.channelName
                                            )
                                        }
                                        CONTACT_CANT_CHANGE_TO_VIDEO -> {
                                            Timber.d("LLAMADA PASO: CONTACT_CANT_CHANGE_TO_VIDEO")
                                            socketEventListener.contactCantChangeToVideoCall(
                                                event.channelName
                                            )
                                        }
                                        CONTACT_TURN_ON_CAMERA -> {
                                            Timber.d("LLAMADA PASO: CONTACT_TURN_ON_CAMERA")
                                            socketEventListener.toggleContactCamera(
                                                event.channelName,
                                                isVisible = true
                                            )
                                        }

                                        CONTACT_TURN_OFF_CAMERA -> {
                                            Timber.d("LLAMADA PASO: CONTACT_TURN_OFF_CAMERA")
                                            socketEventListener.toggleContactCamera(
                                                event.channelName,
                                                isVisible = false
                                            )
                                        }

                                        HANGUP_CALL -> {
                                            Timber.d("LLAMADA PASO: HANGUP_CALL")
                                            socketEventListener.contactHasHangup(event.channelName)
                                        }
                                    }

                                } else {

                                    val jsonData = JSONObject(event.data)

                                    if (jsonData.has(TYPE)) {

                                        when (jsonData.getString(TYPE)) {

                                            ICE_CANDIDATE -> {

                                                Timber.d("LLAMADA PASO: RECEPCION DE ICECANDIDATE RECIBIDO")

                                                socketEventListener.iceCandidateReceived(
                                                    event.channelName,
                                                    jsonData.toIceCandidate()
                                                )

                                            }

                                            OFFER -> {

                                                Timber.d("LLAMADA PASO: OFERTA RECIBIDA")

                                                socketEventListener.offerReceived(
                                                    event.channelName,
                                                    jsonData.toSessionDescription(
                                                        SessionDescription.Type.OFFER
                                                    )
                                                )
                                            }

                                            ANSWER -> {

                                                Timber.d("LLAMADA PASO: RESPUESTA RECIBIDA")

                                                socketEventListener.answerReceived(
                                                    event.channelName,
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
}