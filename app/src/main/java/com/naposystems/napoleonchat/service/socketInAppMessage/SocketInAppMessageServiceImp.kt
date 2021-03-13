package com.naposystems.napoleonchat.service.socketInAppMessage

import android.content.Context
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.syncManager.SyncManager
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesReadedDTO
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesReceivedDTO
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageEventAttachmentRes
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageEventMessageRes
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageEventRes
import com.naposystems.napoleonchat.source.remote.dto.validateMessageEvent.ValidateMessage
import com.naposystems.napoleonchat.source.remote.dto.validateMessageEvent.ValidateMessageEventDTO
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Data
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.pusher.client.Pusher
import com.pusher.client.channel.*
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class SocketInAppMessageServiceImp @Inject constructor(
    private val context: Context,
    private val pusher: Pusher,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val syncManager: SyncManager,
    private val cryptoMessage: CryptoMessage
) : SocketInAppMessageService {

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    private val app: NapoleonApplication by lazy {
        context as NapoleonApplication
    }

    private var userId = syncManager.getUserId()

    private var privateGeneralChannelName: String

    private lateinit var generalChannel: PrivateChannel

    private lateinit var globalChannel: PrivateChannel

    companion object {
        const val HANGUP_CALL = 2
        const val CONTACT_WANT_CHANGE_TO_VIDEO = 3
        const val CONTACT_ACCEPT_CHANGE_TO_VIDEO = 4
        const val CONTACT_TURN_OFF_CAMERA = 5
        const val CONTACT_TURN_ON_CAMERA = 6
        const val CONTACT_CANCEL_CHANGE_TO_VIDEO = 7
        const val CONTACT_CANT_CHANGE_TO_VIDEO = 8
    }

    init {

        privateGeneralChannelName =
            Constants.SocketChannelName.PRIVATE_GENERAL_CHANNEL_NAME.channelName + userId

        Timber.d("Pusher: //////////////////////////////////////")

    }

    //region Metodos Interfaz

    //region Conexion
    override fun getPusherChannel(channel: String): PresenceChannel? =
        pusher.getPresenceChannel(channel)

    override fun connectSocket() {

        Timber.d("Pusher Paso IN: *****************")

        Timber.d("Pusher Paso IN 1: connectSocket: State:${pusher.connection.state}")

        Timber.d("Pusher: userId: $userId")

        loggearGlobalChannel("connectSocket")

        if (userId != Constants.UserNotExist.USER_NO_EXIST.user) {

            if (pusher.connection.state == ConnectionState.DISCONNECTED ||
                pusher.connection.state == ConnectionState.DISCONNECTING
            ) {

                pusher.connect(object : ConnectionEventListener {

                    override fun onConnectionStateChange(change: ConnectionStateChange?) {

                        if (change?.currentState == ConnectionState.CONNECTED) {
                            Timber.d("Pusher Paso IN 2.1: CONNECTED")
                            subscribeChannels()
                        }
                    }

                    override fun onError(message: String?, code: String?, e: java.lang.Exception?) {

                        Timber.d("Pusher Paso IN 2.2: connectSocket: onError $message, code: $code")

                        pusher.connect()

                    }
                })
            } else if (pusher.connection.state == ConnectionState.CONNECTED && app.isAppVisible()) {

                loggearGlobalChannel("Ya Conectado")

//                if (!::globalChannel.isInitialized || !::generalChannel.isInitialized) {

                Timber.d("Pusher Paso IN 2.4: Canales inicializados")

                subscribeChannels()

//                }
            }
        }
    }

    override fun disconnectSocket() {

        Timber.d("Pusher Paso IN 7.1: Desconectar")

        try {

            if (pusher.connection.state == ConnectionState.CONNECTED ||
                pusher.connection.state == ConnectionState.CONNECTING
            ) {
//
//                unsubscribeChannel()

                Timber.d("Pusher Paso IN 7.2: Desconectando")

                pusher.disconnect()

            }
        } catch (e: Exception) {
            Timber.e("Pusher Paso IN 7.3: $e")
        }
    }
    //endregion

    //region Mensajes
    //TODO: Fusionar estos metodos
    private fun emitClientConversation(messages: ValidateMessage) {
        emitClientConversation(arrayListOf(messages))
    }

    override fun emitClientConversation(messages: List<ValidateMessage>) {

        loggearGlobalChannel("emitClientConversation")

        Timber.d("Pusher 6.1: Emitir")

        try {

            val validateMessage = ValidateMessageEventDTO(messages)

            val adapterValidate = moshi.adapter(ValidateMessageEventDTO::class.java)

            val jsonObject = adapterValidate.toJson(validateMessage)

            if (jsonObject.isNotEmpty()) {

                Timber.d("Pusher Paso IN 6.2: Emitir a global")

                globalChannel.trigger(
                    Constants.SocketEmitTriggers.CLIENT_CONVERSATION.trigger,
                    jsonObject
                )
            }


        } catch (e: Exception) {

            loggearGlobalChannel("error emitClientConversation $e")

            Timber.d("Pusher Paso IN 6.3: Error al emitir")

            Timber.e("Pusher Paso IN 6.4: $e}")
        }

    }
    //endregion

    //region llamadas
    override fun connectToSocketReadyForCall(channel: String) {
//        Timber.d("connectToSocket: ${pusher.connection.state}")
//        if (pusher.connection.state != ConnectionState.CONNECTED) {
//            pusher.connect(object : ConnectionEventListener {
//                override fun onConnectionStateChange(change: ConnectionStateChange?) {
//                    when (change?.currentState) {
//                        ConnectionState.CONNECTED -> {
//                            try {
//                                sharedPreferencesManager.putString(
//                                    Constants.SharedPreferences.PREF_SOCKET_ID,
//                                    pusher.connection.socketId
//                                )
//                                Timber.d("Conectó al socket ${pusher.connection.socketId}")
//
//                                subscribeToPrivateGeneralChannel()
//                                subscribeToCallChannelUserAvailableForCall(channel)
//                            } catch (e: Exception) {
//                                Timber.e(e)
//                            }
//                        }
//                        ConnectionState.CONNECTING -> Timber.d("Socket: ConnectionState.CONNECTING")
//                        ConnectionState.DISCONNECTED -> Timber.d("Socket: ConnectionState.DISCONNECTED")
//                        ConnectionState.DISCONNECTING -> Timber.d("Socket: ConnectionState.DISCONNECTING")
//                        ConnectionState.RECONNECTING -> Timber.d("Socket: ConnectionState.RECONNECTING")
//                        else -> Timber.d("Socket Error")
//                    }
//                }
//
//                override fun onError(message: String?, code: String?, e: java.lang.Exception?) {
//                    Timber.e("Pusher onError $message, code: $code")
//                }
//            })
//        }
    }

    override fun subscribeToCallChannel(
        channel: String,
        isActionAnswer: Boolean,
        isVideoCall: Boolean
    ) {
//        if (pusher.getPresenceChannel(channel) == null) {
//            Timber.d("subscribeToCallChannel: $channel")
//            callChannel = pusher.subscribePresence(channel, object : PresenceChannelEventListener {
//                override fun onEvent(event: PusherEvent) {
//                    Timber.d("event: ${event.data}")
//                }
//
//                override fun onAuthenticationFailure(message: String, e: java.lang.Exception) = Unit
//
//                override fun onSubscriptionSucceeded(channelName: String) {
//                    listenCallEvents(callChannel!!)
//                    Data.isOnCall = true
//
//                    Timber.d("Subscribe call channel to $channelName")
//
//                    if (isActionAnswer) {
//                        joinToCall(channel)
//                    }
//                }
//
//                override fun onUsersInformationReceived(
//                    channelName: String?,
//                    users: MutableSet<User>?
//                ) {
//                    Timber.d("onUsersInformationReceived, $channelName, $users")
//                }
//
//                override fun userSubscribed(channelName: String, user: User) {
//                    Timber.d("userSubscribed, $channelName, $user")
//                    if (!Data.isContactReadyForCall) {
//                        Data.isContactReadyForCall = true
//                        repository.readyForCall(
//                            user.id.toInt(),
//                            isVideoCall,
//                            channelName.removePrefix("presence-")
//                        )
//                    }
//                }
//
//                override fun userUnsubscribed(channelName: String?, user: User?) {
//                    Timber.d("userUnsubscribed, $channelName, $user")
//                }
//            })
//        }
    }

    override fun joinToCall(channel: String) {
//        emitToCall(channel, CONTACT_JOIN_TO_CALL)
    }

    override fun emitToCall(channel: String, jsonObject: JSONObject) {
//        callChannel?.trigger(CALL_NN, jsonObject.toString())
//
//        Timber.d("Emit to Call $jsonObject")
    }

    override fun emitToCall(channel: String, eventType: Int) {
//        try {
//            callChannel?.trigger(CALL_NN, eventType.toString())
//
//            Timber.d("Emit to Call $eventType")
//        } catch (e: Exception) {
//            Timber.e(e)
//        }
    }

    override fun unSubscribeCallChannel(channelName: String) {
//        pusher.unsubscribe(channelName)
//        Timber.d("unsubscribe to channel: $channelName")
    }
    //endregion

    //endregion

    //region Metodos Privados
    private fun subscribeChannels() {

        Timber.d("Pusher Paso IN 3: subscribeChannels")

        try {
            sharedPreferencesManager.putString(
                Constants.SharedPreferences.PREF_SOCKET_ID,
                pusher.connection.socketId
            )

            loggearGlobalChannel("subscribeChannels")

            unsubscribeChannel()

            subscribeToPrivateGeneralChannel()

            subscribeToPrivateGlobalChannel()

        } catch (e: Exception) {

            loggearGlobalChannel("Error SubscribeChannels $e")

            Timber.e(e)
        }
    }

    private fun subscribeToPrivateGeneralChannel() {

        loggearGlobalChannel("subscribeToPrivateGeneralChannel")

        Timber.d("Pusher Paso IN 4: subscribeToPrivateGeneralChannel")

        try {

            if (pusher.getPrivateChannel(privateGeneralChannelName) == null) {

                generalChannel = pusher.subscribePrivate(
                    privateGeneralChannelName,
                    object : PrivateChannelEventListener {
                        override fun onEvent(event: PusherEvent) {
                            Timber.d("Pusher Paso IN : subscribeToPrivateGeneralChannel: onEvent ${event.data}")
                        }

                        override fun onAuthenticationFailure(
                            message: String?,
                            e: java.lang.Exception?
                        ) {
                            Timber.d("Pusher Paso IN : subscribeToPrivateGeneralChannel: onAuthenticationFailure")
                        }

                        override fun onSubscriptionSucceeded(channelName: String?) {

                            Timber.d("Pusher Paso IN 4.1: onSubscriptionSucceeded $channelName")

                            //Metodos Generales
                            listenOnDisconnect(generalChannel)

                            //Metodos de mensajes
                            listenNewMessage(generalChannel)

                            listenNotifyMessagesReceived(generalChannel)

                            listenNotifyMessagesRead(generalChannel)

                            listenSendMessagesDestroy(generalChannel)

                            //Metodos de Contactos
                            listenCancelOrRejectFriendshipRequest(generalChannel)

                            listenBLockOrDeleteFriendship(generalChannel)

                            //Metodos de Llamadas
//                            listenUserAvailableForCall(generalChannel)
//
//                            listenIncomingCall(generalChannel)
//
//                            listenRejectedCall(generalChannel)
//
//                            listenCancelCall(generalChannel)

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

        loggearGlobalChannel("subscribeToPrivateGlobalChannel")

        Timber.d("Pusher Paso IN 5: subscribeToPrivateGlobalChannel")

        try {

            if (pusher.getPrivateChannel(Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName) == null) {

                Timber.d("Pusher Paso IN 5.1.d: Entro al if")

                globalChannel = pusher.subscribePrivate(
                    Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName,
                    object : PrivateChannelEventListener {
                        override fun onEvent(event: PusherEvent?) {
                            Timber.d("Pusher Paso IN: subscribeToPrivateGlobalChannel: onEvent ${event?.data}")
                        }

                        override fun onAuthenticationFailure(
                            message: String?,
                            e: java.lang.Exception?
                        ) {
                            Timber.d("Pusher Paso IN: subscribeToPrivateGlobalChannel: onAuthenticationFailure")
                        }

                        override fun onSubscriptionSucceeded(channelName: String?) {

                            Timber.d("Pusher Paso IN 5.2: onSubscriptionSucceeded:$channelName")

                            listenValidateConversationEvent()

                        }
                    }
                )
            } else {

                listenValidateConversationEvent()
            }

        } catch (e: Exception) {

            loggearGlobalChannel("error subscribeToPrivateGlobalChannel $e")

            Timber.e("Pusher Paso IN 5.4:  subscribeToPrivateGlobalChannel: Exception: $e")
        }
    }

    private fun availableToReceived(attachments: List<NewMessageEventAttachmentRes>): Boolean {

        val attachment: NewMessageEventAttachmentRes? = attachments.firstOrNull() {
            it.type == Constants.AttachmentType.IMAGE.type ||
                    it.type == Constants.AttachmentType.AUDIO.type ||
                    it.type == Constants.AttachmentType.VIDEO.type ||
                    it.type == Constants.AttachmentType.DOCUMENT.type
        }

        return attachment != null

    }

    private fun unsubscribeChannel() {

        Timber.d("Pusher Paso IN 3.1: Desubscribirse")

//        if (::globalChannel.isInitialized)
//            if (pusher.getPrivateChannel(Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName) != null)
//                if (pusher.getPrivateChannel(Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName).isSubscribed)
        pusher.unsubscribe(Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName)

//        if (::generalChannel.isInitialized)
//            if (pusher.getPrivateChannel(Constants.SocketChannelName.PRIVATE_GENERAL_CHANNEL_NAME.channelName + userId) != null)
//                if (pusher.getPrivateChannel(Constants.SocketChannelName.PRIVATE_GENERAL_CHANNEL_NAME.channelName + userId).isSubscribed)
        pusher.unsubscribe(privateGeneralChannelName)

    }

    private fun unbindChannel(channel: PrivateChannel, channelName: String) {
        channel.unbind(channelName, SubscriptionEventListener { })
    }

    //region Region Escuchadores de Eventos

    //region Metodos Conexion
    private fun listenOnDisconnect(privateChannel: PrivateChannel) {

        unbindChannel(privateChannel, Constants.SocketListeEvents.DISCONNECT.channel)

        privateChannel.bind(
            Constants.SocketListeEvents.DISCONNECT.channel,
            object : PrivateChannelEventListener {
                override fun onEvent(event: PusherEvent?) {
                    Timber.d("Socket disconnect ${event?.data}")
                    Timber.e("Pusher Paso IN 8.1: Desconectado")
                    pusher.connect()
                }

                override fun onAuthenticationFailure(message: String?, e: java.lang.Exception?) =
                    Unit

                override fun onSubscriptionSucceeded(channelName: String?) = Unit
            })
    }
    //endregion

    //region Metodos de Mensajes
    private fun listenNewMessage(privateChannel: PrivateChannel) {

        unbindChannel(privateChannel, Constants.SocketListeEvents.NEW_MESSAGE.channel)

        privateChannel.bind(Constants.SocketListeEvents.NEW_MESSAGE.channel,
            object : PrivateChannelEventListener {
                override fun onEvent(event: PusherEvent?) {

                    Timber.d("Pusher: listenNewMessage:${event?.data}")

                    if (app.isAppVisible()) {

                        Timber.d("Pusher: appVisible")

                        try {

                            event?.data?.let { dataEventRes ->

                                val jsonAdapterData: JsonAdapter<NewMessageEventRes> =
                                    moshi.adapter(NewMessageEventRes::class.java)

                                val dataEvent = jsonAdapterData.fromJson(dataEventRes)

                                dataEvent?.data?.let { newMessageDataEventRes ->

                                    val message =
                                        ValidateMessage(
                                            id = newMessageDataEventRes.messageId,
                                            user = newMessageDataEventRes.contactId,
                                            status = Constants.MessageEventType.UNREAD.status
                                        )

                                    syncManager.insertNewMessage(newMessageDataEventRes)

                                    val data: String = if (BuildConfig.ENCRYPT_API) {
                                        cryptoMessage.decryptMessageBody(newMessageDataEventRes.message)
                                    } else {
                                        newMessageDataEventRes.message
                                    }

                                    val jsonAdapterMessage: JsonAdapter<NewMessageEventMessageRes> =
                                        moshi.adapter(NewMessageEventMessageRes::class.java)

                                    jsonAdapterMessage.fromJson(data)
                                        ?.let { newMessageEventMessageRes ->

                                            if (newMessageEventMessageRes.numberAttachments > 0) {
                                                if ((availableToReceived(newMessageEventMessageRes.attachments) && Data.contactId == newMessageEventMessageRes.userAddressee) ||
                                                    Data.contactId == 0
                                                ) {

                                                    syncManager.notifyMessageReceived(message.id)

                                                    emitClientConversation(message)

                                                }
                                            } else if (Data.contactId != newMessageEventMessageRes.userAddressee) {

                                                syncManager.notifyMessageReceived(message.id)

                                                emitClientConversation(message)

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

    private fun listenNotifyMessagesReceived(privateChannel: PrivateChannel) {

        unbindChannel(privateChannel, Constants.SocketListeEvents.NOTIFY_MESSAGES_RECEIVED.channel)

        privateChannel.bind(Constants.SocketListeEvents.NOTIFY_MESSAGES_RECEIVED.channel,
            object : PrivateChannelEventListener {
                override fun onEvent(event: PusherEvent?) {
                    Timber.d("NotifyMessagesReceived: ${event?.data}")
                    event?.data?.let {
                        val jsonAdapter: JsonAdapter<MessagesReceivedDTO> =
                            moshi.adapter(MessagesReceivedDTO::class.java)

                        val dataEvent = jsonAdapter.fromJson(it)

                        dataEvent?.let { messagesReceivedDTO ->

                            Timber.d(messagesReceivedDTO.data.messageIds.toString())

                            syncManager.updateMessagesStatus(
                                messagesReceivedDTO.data.messageIds,
                                Constants.MessageStatus.UNREAD.status
                            )
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

    private fun listenNotifyMessagesRead(privateChannel: PrivateChannel) {

        unbindChannel(privateChannel, Constants.SocketListeEvents.NOTIFY_MESSAGE_READED.channel)

        privateChannel.bind(Constants.SocketListeEvents.NOTIFY_MESSAGE_READED.channel,
            object : PrivateChannelEventListener {
                override fun onEvent(event: PusherEvent?) {
                    Timber.d("NotifyMessageReaded: ${event?.data}")

                    event?.data?.let {
                        val jsonAdapter: JsonAdapter<MessagesReadedDTO> =
                            moshi.adapter(MessagesReadedDTO::class.java)

                        val dataEvent = jsonAdapter.fromJson(it)

                        dataEvent?.let { messagesReadedDTO ->

                            Timber.d(messagesReadedDTO.data.messageIds.toString())

                            syncManager.updateMessagesStatus(
                                messagesReadedDTO.data.messageIds,
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

    private fun listenSendMessagesDestroy(privateChannel: PrivateChannel) {

        unbindChannel(privateChannel, Constants.SocketListeEvents.SEND_MESSAGES_DESTROY.channel)

        privateChannel.bind(Constants.SocketListeEvents.SEND_MESSAGES_DESTROY.channel,
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

        loggearGlobalChannel("listenValidateConversationEvent")

        unbindChannel(globalChannel, Constants.SocketEmitTriggers.CLIENT_CONVERSATION.trigger)

        globalChannel.bind(
            Constants.SocketEmitTriggers.CLIENT_CONVERSATION.trigger,
            object : PrivateChannelEventListener {
                override fun onEvent(event: PusherEvent?) {
                    try {
                        event?.data?.let { dataEventRes ->

                            val jsonAdapter: JsonAdapter<ValidateMessageEventDTO> =
                                moshi.adapter(ValidateMessageEventDTO::class.java)

                            val dataEvent = jsonAdapter.fromJson(dataEventRes)

                            val messages = dataEvent?.messages?.filter {
                                it.user == userId
                            }?.filter {
                                syncManager.existIdMessage(it.id)
                            }

                            val unread = messages?.filter {
                                it.status == Constants.MessageEventType.UNREAD.status
                            }?.map { it.id }

                            unread?.let {
                                syncManager.updateMessagesStatus(
                                    it,
                                    Constants.MessageStatus.UNREAD.status
                                )
                            }

                            val read = messages?.filter {
                                it.status == Constants.MessageEventType.READ.status
                            }?.map { it.id }

                            read?.let {
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

                override fun onAuthenticationFailure(message: String?, e: java.lang.Exception?) =
                    Unit
            }
        )
    }
    //endregion

    //region Metodos de Contactos
    private fun listenCancelOrRejectFriendshipRequest(privateChannel: PrivateChannel) {

        unbindChannel(
            privateChannel,
            Constants.SocketListeEvents.CANCEL_OR_REJECT_FRIENDSHIP_REQUEST.channel
        )

        privateChannel.bind(Constants.SocketListeEvents.CANCEL_OR_REJECT_FRIENDSHIP_REQUEST.channel,
            object : PrivateChannelEventListener {
                override fun onEvent(event: PusherEvent?) {
                    RxBus.publish(RxEvent.CancelOrRejectFriendshipRequestEvent())
                }

                override fun onSubscriptionSucceeded(channelName: String?) = Unit

                override fun onAuthenticationFailure(
                    message: String?,
                    e: java.lang.Exception?
                ) =
                    Unit
            }
        )
    }

    private fun listenBLockOrDeleteFriendship(privateChannel: PrivateChannel) {

        unbindChannel(
            privateChannel,
            Constants.SocketListeEvents.BLOCK_OR_DELETE_FRIENDSHIP.channel
        )

        privateChannel.bind(Constants.SocketListeEvents.BLOCK_OR_DELETE_FRIENDSHIP.channel,
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
                ) =
                    Unit

                override fun onSubscriptionSucceeded(channelName: String?) = Unit

            })
    }
    //endregion

    //endregion

    //endregion
    private fun loggearGlobalChannel(location: String) {

        Timber.d("Pusher Paso IN 5.1: ************************************")

        Timber.d("Pusher Paso IN 5.1: Global Channel Es Inicializado: ${::globalChannel.isInitialized} en $location")

        if (::globalChannel.isInitialized)
            Timber.d("Pusher Paso IN 5.1: Global Channel Es Suscrito: ${globalChannel.isSubscribed} en $location")

        Timber.d(
            "Pusher Paso IN 5.1: Canal Global de Pusher Existe: ${
                pusher.getPrivateChannel(
                    Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName
                )
            } en $location"
        )

        if (pusher.getPrivateChannel(Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName) != null)
            Timber.d(
                "Pusher Paso IN 5.1: Canal Global de Pusher Es Suscrito: ${
                    pusher.getPrivateChannel(
                        Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName
                    ).isSubscribed
                } en $location"
            )

    }

}