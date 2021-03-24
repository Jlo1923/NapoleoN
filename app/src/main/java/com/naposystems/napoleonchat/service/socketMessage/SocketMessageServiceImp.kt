package com.naposystems.napoleonchat.service.socketMessage

import android.content.Context
import android.content.Intent
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.model.conversationCall.IncomingCall
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.notificationMessage.NotificationMessagesServiceImp
import com.naposystems.napoleonchat.service.syncManager.SyncManager
import com.naposystems.napoleonchat.service.webRTCCall.WebRTCCallService
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
import com.naposystems.napoleonchat.utility.adapters.toIceCandidate
import com.naposystems.napoleonchat.utility.adapters.toSessionDescription
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

class SocketMessageServiceImp @Inject constructor(
    private val context: Context,
    private val pusher: Pusher,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val syncManager: SyncManager,
    private val cryptoMessage: CryptoMessage
) : SocketMessageService {

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    private val app: NapoleonApplication by lazy {
        context as NapoleonApplication
    }

    private var userId: Int = Constants.UserNotExist.USER_NO_EXIST.user

    private lateinit var privateGeneralChannelName: String

    private lateinit var socketEventListenerCall: SocketEventsListener.Call

    companion object {
        const val CALL_NN = "client-callNN"
        const val CONTACT_JOIN_TO_CALL = 1
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

    override fun setSocketCallListener(socketEventsListenerCall: SocketEventsListener.Call) {
        this.socketEventListenerCall = socketEventsListenerCall
    }

    //region Conexion
    override fun getPusherChannel(channel: String): PresenceChannel? =
        pusher.getPresenceChannel(channel)

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

        userId = syncManager.getUserId()

        if (userId != Constants.UserNotExist.USER_NO_EXIST.user) {

            privateGeneralChannelName =
                Constants.SocketChannelName.PRIVATE_GENERAL_CHANNEL_NAME.channelName + userId

            if (pusher.connection.state == ConnectionState.DISCONNECTED ||
                pusher.connection.state == ConnectionState.DISCONNECTING
            ) {

                pusher.connect(object : ConnectionEventListener {

                    override fun onConnectionStateChange(change: ConnectionStateChange?) {
                        if (change?.currentState == ConnectionState.CONNECTED) {
                            subscribeChannels()
                        }
                    }

                    override fun onError(message: String?, code: String?, e: java.lang.Exception?) {
                        pusher.connect()
                    }
                })
            } else if (pusher.connection.state == ConnectionState.CONNECTED && app.isAppVisible()) {
                subscribeChannels()
            }
        }
    }

    override fun disconnectSocket() {

        try {

            if (pusher.connection.state == ConnectionState.CONNECTED ||
                pusher.connection.state == ConnectionState.CONNECTING
            ) {

                if (pusher.getPrivateChannel(Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName) != null) {

                    //Unbind Global

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

                if (pusher.getPrivateChannel(privateGeneralChannelName) != null) {

                    //Unbind General

                    pusher.getPrivateChannel(privateGeneralChannelName)
                        .unbind(Constants.SocketListeEvents.DISCONNECT.channel,
                            SubscriptionEventListener {}
                        )

                    pusher.getPrivateChannel(privateGeneralChannelName)
                        .unbind(Constants.SocketListeEvents.NEW_MESSAGE.channel,
                            SubscriptionEventListener {}
                        )

                    pusher.getPrivateChannel(privateGeneralChannelName)
                        .unbind(Constants.SocketListeEvents.NOTIFY_MESSAGES_RECEIVED.channel,
                            SubscriptionEventListener {}
                        )

                    pusher.getPrivateChannel(privateGeneralChannelName)
                        .unbind(Constants.SocketListeEvents.NOTIFY_MESSAGE_READED.channel,
                            SubscriptionEventListener {}
                        )

                    pusher.getPrivateChannel(privateGeneralChannelName)
                        .unbind(Constants.SocketListeEvents.SEND_MESSAGES_DESTROY.channel,
                            SubscriptionEventListener {}
                        )

                    pusher.getPrivateChannel(privateGeneralChannelName)
                        .unbind(Constants.SocketListeEvents.CANCEL_OR_REJECT_FRIENDSHIP_REQUEST.channel,
                            SubscriptionEventListener {}
                        )

                    pusher.getPrivateChannel(privateGeneralChannelName)
                        .unbind(Constants.SocketListeEvents.BLOCK_OR_DELETE_FRIENDSHIP.channel,
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

    //TODO: Fusionar estos metodos
    private fun emitClientConversation(messages: ValidateMessage) {
        emitClientConversation(arrayListOf(messages))
    }

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
    //endregion

    //region Metodos Privados
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

                            listenCallRejected()

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
                            if (!app.isAppVisible())
                                RxBus.publish(RxEvent.CreateNotification())
                        }
                    }
                )
            }

        } catch (e: Exception) {
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
    //endregion

    // region Region Escuchadores de Eventos
    private fun listenDisconnect() {

        pusher
            .getPrivateChannel(privateGeneralChannelName)
            .bind(
                Constants.SocketListeEvents.DISCONNECT.channel,
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

        pusher
            .getPrivateChannel(privateGeneralChannelName)
            .bind(
                Constants.SocketListeEvents.NEW_MESSAGE.channel,
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
                                                    if ((availableToReceived(
                                                            newMessageEventMessageRes.attachments
                                                        ) && Data.contactId == newMessageEventMessageRes.userAddressee) ||
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

    private fun listenNotifyMessagesReceived() {

        pusher
            .getPrivateChannel(privateGeneralChannelName)
            .bind(
                Constants.SocketListeEvents.NOTIFY_MESSAGES_RECEIVED.channel,
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

    private fun listenNotifyMessagesRead() {

        pusher
            .getPrivateChannel(privateGeneralChannelName)
            .bind(
                Constants.SocketListeEvents.NOTIFY_MESSAGE_READED.channel,
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

    private fun listenSendMessagesDestroy() {

        pusher
            .getPrivateChannel(privateGeneralChannelName)
            .bind(
                Constants.SocketListeEvents.SEND_MESSAGES_DESTROY.channel,
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

        pusher
            .getPrivateChannel(Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName)
            .bind(
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

                    override fun onAuthenticationFailure(
                        message: String?,
                        e: java.lang.Exception?
                    ) = Unit
                }
            )
    }

    private fun listenCancelOrRejectFriendshipRequest() {

        pusher
            .getPrivateChannel(privateGeneralChannelName)
            .bind(
                Constants.SocketListeEvents.CANCEL_OR_REJECT_FRIENDSHIP_REQUEST.channel,
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

        pusher
            .getPrivateChannel(privateGeneralChannelName)
            .bind(
                Constants.SocketListeEvents.BLOCK_OR_DELETE_FRIENDSHIP.channel,
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

    //endregion

////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////


    private fun listenIncomingCall() {

        pusher
            .getPrivateChannel(privateGeneralChannelName).bind(
                "App\\Events\\CallFriendEvent",
                object : PrivateChannelEventListener {
                    override fun onEvent(event: PusherEvent) {
                        if ((context as NapoleonApplication).isAppVisible()) {

                            try {
                                val moshi = Moshi.Builder().build()

                                val adapter: JsonAdapter<IncomingCall> =
                                    moshi.adapter(IncomingCall::class.java)

                                adapter.fromJson(event.data)?.let { incomingCall ->

                                    val channel = "presence-${incomingCall.data.channel}"

                                    val isOnCallPref = Data.isOnCall

                                    Timber.d("IsOnCall: $isOnCallPref")

                                    if (isOnCallPref) {
                                        syncManager.rejectCall(
                                            incomingCall.data.contactId,
                                            channel
                                        )
                                    } else {
                                        subscribeToCallChannelUserAvailableForCall(
                                            channel,
                                            incomingCall.data.contactId,
                                            incomingCall.data.isVideoCall,
                                            incomingCall.data.offer
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

    private fun listenCallRejected() {

        pusher
            .getPrivateChannel(privateGeneralChannelName).bind(
                "App\\Events\\RejectedCallEvent",
                object : PrivateChannelEventListener {
                    override fun onEvent(event: PusherEvent) {
                        Data.isShowingCallActivity = false
                        Timber.d("RejectedCallEvent: ${event.data}")
//                        RxBus.publish(RxEvent.ContactRejectCall(event.channelName))
                        socketEventListenerCall.contactRejectCall(event.channelName)
                    }

                    override fun onAuthenticationFailure(
                        message: String?,
                        e: java.lang.Exception?
                    ) = Unit

                    override fun onSubscriptionSucceeded(channelName: String?) = Unit
                })
    }

    private fun listenCancelCall() {

        pusher
            .getPrivateChannel(privateGeneralChannelName).bind(
                "App\\Events\\CancelCallEvent",
                object : PrivateChannelEventListener {
                    override fun onEvent(event: PusherEvent) {
                        try {
                            Data.isShowingCallActivity = false
                            Timber.d("CancelCallEvent: ${event.data}, notificationId: ${NotificationMessagesServiceImp.NOTIFICATION_RINGING}")
                            val jsonObject = JSONObject(event.data)
                            if (jsonObject.has("data")) {
                                val jsonData = jsonObject.getJSONObject("data")
                                if (jsonData.has("channel_private")) {
                                    val privateChannel = jsonData.getString("channel_private")
//                                    RxBus.publish(RxEvent.ContactCancelCall(privateChannel))
                                    socketEventListenerCall.contactCancelCall(privateChannel)
                                }
                            }
                            val intent = Intent(context, WebRTCCallService::class.java)
                            intent.action = WebRTCCallService.ACTION_CALL_END
                            context.startService(intent)
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

    override fun subscribeToCallChannel(
        contactId: Int,
        channel: String,
        isActionAnswer: Boolean,
        isVideoCall: Boolean
    ) {
        Timber.d("subscribeToCallChannel: $channel")
        if (pusher.getPresenceChannel(channel) == null) {
            pusher.subscribePresence(
                channel,
                object : PresenceChannelEventListener {
                    override fun onEvent(event: PusherEvent) {
                        Timber.d("event: ${event.data}")
                    }

                    override fun onAuthenticationFailure(
                        message: String,
                        e: java.lang.Exception
                    ) {
                        Timber.e(e, message)
                    }

                    override fun onSubscriptionSucceeded(channelName: String) {

                        if (context is NapoleonApplication) {

                            if (app.isAppVisible()) {

                                listenCallEvents(channelName)

                                Timber.d("onSubscriptionSucceeded: $channelName")

                                Data.isOnCall = true


                                socketEventListenerCall.itsSubscribedToCallChannel(
                                    contactId,
                                    channelName,
                                    isVideoCall
                                )

//                                RxBus.publish(
//                                    RxEvent.ItsSubscribedToCallChannel(
//                                        channelName,
//                                        contactId,
//                                        isVideoCall
//                                    )
//                                )
                            }
                        }
                    }

                    override fun onUsersInformationReceived(
                        channelName: String?,
                        users: MutableSet<User>?
                    ) {
                        Timber.d("onUsersInformationReceived, $channelName, $users")
                    }

                    override fun userSubscribed(channelName: String?, user: User?) {
                        Timber.d("userSubscribed, $channelName, $user")
                    }

                    override fun userUnsubscribed(channelName: String?, user: User?) {
                        Timber.d("userUnsubscribed, $channelName, $user")
                    }
                })
        }
    }

    override fun joinToCall(channel: String) {
        emitToCall(channel, CONTACT_JOIN_TO_CALL)
    }

    override fun emitToCall(channel: String, jsonObject: JSONObject) {

        if (pusher.getPresenceChannel(channel) == null) {

            pusher.getPresenceChannel(channel)
                .trigger(CALL_NN, jsonObject.toString())

            Timber.d("Emit to Call $jsonObject")

        }

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
        try {

            pusher.unsubscribe(channelName)
            Timber.d("unsubscribe to channel: $channelName")

        } catch (e: Exception) {
            Timber.e(e.localizedMessage)
        }
    }

    private fun listenCallEvents(channelName: String) {

        try {

            if (pusher.getPresenceChannel(channelName) == null) {

                pusher
                    .getPresenceChannel(channelName)
                    .bind(CALL_NN, object : PresenceChannelEventListener {

                        override fun onEvent(event: PusherEvent) {

                            try {
                                val eventType = event.data.toIntOrNull()

                                if (eventType != null) {
                                    Timber.d("LLegó $CALL_NN $eventType")

                                    when (eventType) {

                                        CONTACT_WANT_CHANGE_TO_VIDEO ->
                                            socketEventListenerCall.contactWantChangeToVideoCall(
                                                event.channelName
                                            )

                                        CONTACT_ACCEPT_CHANGE_TO_VIDEO ->
                                            socketEventListenerCall.contactAcceptChangeToVideoCall(
                                                event.channelName
                                            )

                                        CONTACT_CANCEL_CHANGE_TO_VIDEO ->
                                            socketEventListenerCall.contactCancelChangeToVideoCall(
                                                event.channelName
                                            )

                                        CONTACT_CANT_CHANGE_TO_VIDEO ->
                                            socketEventListenerCall.contactCantChangeToVideoCall(
                                                event.channelName
                                            )

                                        CONTACT_TURN_ON_CAMERA ->
                                            socketEventListenerCall.contactTurnOnCamera(event.channelName)

                                        CONTACT_TURN_OFF_CAMERA ->
                                            socketEventListenerCall.contactTurnOffCamera(event.channelName)

                                        HANGUP_CALL -> {
                                            Data.isShowingCallActivity = false
                                            socketEventListenerCall.contactHasHangup(event.channelName)
                                        }
                                    }
                                } else {

                                    val jsonData = JSONObject(event.data)

                                    Timber.d("LLegó $CALL_NN $jsonData")

                                    if (jsonData.has(TYPE)) {

                                        when (jsonData.getString(TYPE)) {
                                            ICE_CANDIDATE ->
                                                socketEventListenerCall.iceCandidateReceived(
                                                    event.channelName,
                                                    jsonData.toIceCandidate()
                                                )

                                            OFFER ->
                                                socketEventListenerCall.offerReceived(
                                                    event.channelName,
                                                    jsonData.toSessionDescription(
                                                        SessionDescription.Type.OFFER
                                                    )
                                                )

                                            ANSWER ->
                                                socketEventListenerCall.answerReceived(
                                                    event.channelName,
                                                    jsonData.toSessionDescription(
                                                        SessionDescription.Type.ANSWER
                                                    )
                                                )
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
                        ) {
                            Timber.d("onAuthenticationFailure, $message")
                        }

                        override fun onSubscriptionSucceeded(channelName: String?) {
                            Timber.d("onSubscriptionSucceeded, $channelName")
                        }

                        override fun onUsersInformationReceived(
                            channelName: String?,
                            users: MutableSet<User>?
                        ) {
                            Timber.d("onUsersInformationReceived, $channelName, $users")
                        }

                        override fun userSubscribed(channelName: String?, user: User?) {
                            Timber.d("userSubscribed, $channelName, $user")
                        }

                        override fun userUnsubscribed(channelName: String?, user: User?) {
                            Timber.d("userUnsubscribed, $channelName, $user")
                        }
                    })
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun subscribeToCallChannelUserAvailableForCall(
        channel: String,
        contactId: Int,
        isVideoCall: Boolean,
        offer: String
    ) {
        Timber.d("subscribeToCallChannel: $channel")
        pusher.unsubscribe(channel)
        if (pusher.getPresenceChannel(channel) == null) {
            pusher.subscribePresence(channel, object : PresenceChannelEventListener {
                override fun onEvent(event: PusherEvent) {
                    Timber.d("event: ${event.data}")
                }

                override fun onAuthenticationFailure(
                    message: String,
                    e: java.lang.Exception
                ) = Unit

                override fun onSubscriptionSucceeded(channelName: String) {
                    pusher.getPresenceChannel(channel)?.let {
                        Timber.d("onSubscriptionSucceeded: $channelName")
                        if (it.users.size >= 2) {
                            if (context is NapoleonApplication) {
                                val app = context
                                if (app.isAppVisible()) {
                                    listenCallEvents(channel)
                                    Data.isOnCall = true
                                    RxBus.publish(
                                        RxEvent.IncomingCall(
                                            channel,
                                            contactId,
                                            isVideoCall,
                                            offer
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                override fun onUsersInformationReceived(
                    channelName: String?,
                    users: MutableSet<User>?
                ) {
                    Timber.d("onUsersInformationReceived, $channelName, $users")
                }

                override fun userSubscribed(channelName: String?, user: User?) {
                    Timber.d("userSubscribed, $channelName, $user")
                }

                override fun userUnsubscribed(channelName: String?, user: User?) {
                    Timber.d("userUnsubscribed, $channelName, $user")
                }
            })
        }
    }

    override fun subscribeToCallChannelFromBackground(channel: String) {
        Timber.d("subscribeToCallChannelFromBackground: $channel")
        pusher.unsubscribe(channel)
        if (pusher.getPresenceChannel(channel) == null) {
            pusher.subscribePresence(channel, object : PresenceChannelEventListener {
                override fun onEvent(event: PusherEvent) {
                    Timber.d("event: ${event.data}")
                }

                override fun onAuthenticationFailure(
                    message: String,
                    e: java.lang.Exception
                ) = Unit

                override fun onSubscriptionSucceeded(channelName: String) {
                    pusher.getPresenceChannel(channel).let {
                        Timber.d("onSubscriptionSucceeded: $channelName")
                        listenCallEvents(channel)
                    }
                }

                override fun onUsersInformationReceived(
                    channelName: String?,
                    users: MutableSet<User>?
                ) {
                    Timber.d("onUsersInformationReceived, $channelName, $users")
                }

                override fun userSubscribed(channelName: String?, user: User?) {
                    Timber.d("userSubscribed, $channelName, $user")
                }

                override fun userUnsubscribed(channelName: String?, user: User?) {
                    Timber.d("userUnsubscribed, $channelName, $user")
                }
            })
        }
    }

}