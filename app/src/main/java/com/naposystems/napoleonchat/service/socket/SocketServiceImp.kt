package com.naposystems.napoleonchat.service.socket

import android.content.Context
import android.content.Intent
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.model.conversationCall.IncomingCall
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.notification.NotificationService
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

class SocketServiceImp @Inject constructor(
    private val context: Context,
    private val pusher: Pusher,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val syncManager: SyncManager,
    private val cryptoMessage: CryptoMessage
) : SocketService {

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    private val app: NapoleonApplication by lazy {
        context as NapoleonApplication
    }

    private var userId = syncManager.getUserId()

    private var privateGeneralChannelName: String

    private var privateGlobalChannelName: String

    private var locationConnectSocket: Boolean = Constants.LocationConnectSocket.FROM_APP.location

    private lateinit var generalChannel: PrivateChannel

    private lateinit var globalChannel: PrivateChannel

    private var callChannel: PresenceChannel? = null

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

        const val CLIENT_CONVERSATION_NN = "client-conversationNN"
    }

    init {

        privateGeneralChannelName = "private-general.${userId}"

        privateGlobalChannelName = "private-global"

        Timber.d("Pusher: //////////////////////////////////////")

    }

    //region Implementacion Socket Mensajes
    override fun connectSocket(locationConnectSocket: Boolean) {
        connectSocket(locationConnectSocket, null)
    }

    override fun connectSocket(locationConnectSocket: Boolean, socketCallback: SocketCallback?) {

        this.locationConnectSocket = locationConnectSocket

        Timber.d("Pusher: *****************")

        Timber.d("Pusher: connectSocket: State:${pusher.connection.state}")

        if (userId != Constants.UserNotExist.USER_NO_EXIST.user) {

            if (pusher.connection.state == ConnectionState.DISCONNECTED ||
                pusher.connection.state == ConnectionState.DISCONNECTING
            ) {

                pusher.connect(object : ConnectionEventListener {

                    override fun onConnectionStateChange(change: ConnectionStateChange?) {

                        if (change?.currentState == ConnectionState.CONNECTED)
                            subscribeChannels()
                        else
                            Timber.d("Pusher: connectSocket: State:${pusher.connection.state}")

                    }

                    override fun onError(message: String?, code: String?, e: java.lang.Exception?) {

                        Timber.d("Pusher: connectSocket: onError $message, code: $code")

                        pusher.connect()

                    }

                })
            } else if (pusher.connection.state == ConnectionState.CONNECTED &&
                locationConnectSocket == Constants.LocationConnectSocket.FROM_NOTIFICATION.location &&
                !app.isAppVisible()
            ) {
                subscribeChannels()
            }
        }
    }

    override fun disconnectSocket() {

        try {

            if (pusher.connection.state == ConnectionState.CONNECTED ||
                pusher.connection.state == ConnectionState.CONNECTING
            ) {
                pusher.disconnect()
                Timber.d("Pusher: disconnectSocket")
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun emitClientConversation(messages: ValidateMessage) {
        emitClientConversation(arrayListOf(messages))
    }

    override fun emitClientConversation(messages: List<ValidateMessage>) {

        try {

            val validateMessage = ValidateMessageEventDTO(messages)

            val adapterValidate = moshi.adapter(ValidateMessageEventDTO::class.java)

            val jsonObject = adapterValidate.toJson(validateMessage)

            if (jsonObject.isNotEmpty())
                globalChannel.trigger(CLIENT_CONVERSATION_NN, jsonObject)

        } catch (e: Exception) {
            Timber.e(e)
        }

    }

    //endregion

    //region Metodos Privados
    private fun subscribeChannels() {

        Timber.d("Pusher: subscribeChannels")

        try {
            sharedPreferencesManager.putString(
                Constants.SharedPreferences.PREF_SOCKET_ID,
                pusher.connection.socketId
            )

            Timber.d("Pusher: State:${pusher.connection.state}, SocketId:${pusher.connection.socketId}")

            pusher.unsubscribe(privateGeneralChannelName)

            pusher.unsubscribe(privateGlobalChannelName)

            if (locationConnectSocket == Constants.LocationConnectSocket.FROM_APP.location) {
                subscribeToPrivateGeneralChannel()
            }

            subscribeToPrivateGlobalChannel()

        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun subscribeToPrivateGeneralChannel() {

        Timber.d("Pusher: subscribeToPrivateGeneralChannel: instance:$this")

        Timber.d(
            "Pusher: subscribeToPrivateGeneralChannel:  privateGeneralChannelName: ${
                pusher.getPrivateChannel(
                    privateGeneralChannelName
                )
            }"
        )

        try {

            if (pusher.getPrivateChannel(privateGeneralChannelName) == null) {

                Timber.d(
                    "Pusher: subscribeToPrivateGeneralChannel:  IF privateGeneralChannelName: ${
                        pusher.getPrivateChannel(
                            privateGeneralChannelName
                        )
                    }"
                )

                generalChannel = pusher.subscribePrivate(
                    privateGeneralChannelName,
                    object : PrivateChannelEventListener {
                        override fun onEvent(event: PusherEvent) {
                            Timber.d("Pusher: subscribeToPrivateGeneralChannel: onEvent ${event.data}")
                        }

                        override fun onAuthenticationFailure(
                            message: String?,
                            e: java.lang.Exception?
                        ) {
                            Timber.d("Pusher: subscribeToPrivateGeneralChannel: onAuthenticationFailure")
                        }

                        override fun onSubscriptionSucceeded(channelName: String?) {

                            Timber.d("Pusher: subscribeToPrivateGeneralChannel: fromApp onSubscriptionSucceeded: $channelName")

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
                            listenUserAvailableForCall(generalChannel)

                            listenIncomingCall(generalChannel)

                            listenRejectedCall(generalChannel)

                            listenCancelCall(generalChannel)

                            syncManager.getMyMessages(null)

                            syncManager.verifyMessagesReceived()

                            syncManager.verifyMessagesRead()

                        }

                    })
            }

        } catch (e: Exception) {
            Timber.e("Pusher:  subscribeToPrivateGeneralChannel: Exception: $e")
        }
    }

    private fun subscribeToPrivateGlobalChannel() {

        Timber.d(
            "Pusher: subscribeToPrivateGlobalChannel: instance:$this privateGlobalChannelName: ${
                pusher.getPrivateChannel(
                    privateGlobalChannelName
                )
            }"
        )

        try {

            if (pusher.getPrivateChannel(privateGlobalChannelName) == null) {

                Timber.d(
                    "Pusher: subscribeToPrivateGlobalChannel:  IF privateGlobalChannelName: ${
                        pusher.getPrivateChannel(
                            privateGlobalChannelName
                        )
                    }"
                )

                globalChannel = pusher.subscribePrivate(
                    privateGlobalChannelName,
                    object : PrivateChannelEventListener {
                        override fun onEvent(event: PusherEvent?) {
                            Timber.d("Pusher: subscribeToPrivateGlobalChannel: onEvent ${event?.data}")
                        }

                        override fun onAuthenticationFailure(
                            message: String?,
                            e: java.lang.Exception?
                        ) {
                            Timber.d("Pusher: subscribeToPrivateGlobalChannel: onAuthenticationFailure")
                        }

                        override fun onSubscriptionSucceeded(channelName: String?) {

                            Timber.d("Pusher: subscribeToPrivateGlobalChannel: onSubscriptionSucceeded:$channelName")

                            if (locationConnectSocket == Constants.LocationConnectSocket.FROM_NOTIFICATION.location) {

//                                RxBus.publish(RxEvent.CreateNotification())

                            } else if (locationConnectSocket == Constants.LocationConnectSocket.FROM_APP.location) {
                                listenValidateConversationEvent()
                            }
                        }
                    }
                )
            }

        } catch (e: Exception) {
            Timber.e("Pusher:  subscribeToPrivateGlobalChannel: Exception: $e")
        }
    }

    private fun availableToReceived(attachments: List<NewMessageEventAttachmentRes>): Boolean {

        var attachment: NewMessageEventAttachmentRes? = attachments.firstOrNull() {
            it.type == Constants.AttachmentType.IMAGE.type ||
                    it.type == Constants.AttachmentType.AUDIO.type ||
                    it.type == Constants.AttachmentType.VIDEO.type ||
                    it.type == Constants.AttachmentType.DOCUMENT.type
        }

        return attachment != null

    }

    fun subscribeToCallChannelUserAvailableForCall(channel: String) {

        Timber.d("subscribeToCallChannel: $channel")

        if (pusher.getPresenceChannel(channel) == null) {

            callChannel =
                pusher.subscribePresence(channel, object : PresenceChannelEventListener {
                    override fun onEvent(event: PusherEvent) {
                        Timber.d("event: ${event.data}")
                    }

                    override fun onAuthenticationFailure(
                        message: String,
                        e: java.lang.Exception
                    ) = Unit

                    override fun onSubscriptionSucceeded(channelName: String) {
                        Timber.d("onSubscriptionSucceeded: $channelName")
                        if (pusher.getPresenceChannel(channelName) != null) {
                            pusher.unsubscribe(channelName)
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
    //endregion

    //region Region Escuchadores de Eventos

    //region Metodos Generales
    private fun listenOnDisconnect(privateChannel: PrivateChannel) {

        unbindChannel(privateChannel, Constants.EventsSocket.DISCONNECT.channel)

        privateChannel.bind(
            Constants.EventsSocket.DISCONNECT.channel,
            object : PrivateChannelEventListener {
                override fun onEvent(event: PusherEvent?) {
                    Timber.d("Socket disconnect ${event?.data}")
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

        unbindChannel(privateChannel, Constants.EventsSocket.NEW_MESSAGE.channel)

        privateChannel.bind(Constants.EventsSocket.NEW_MESSAGE.channel,
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

        unbindChannel(privateChannel, Constants.EventsSocket.NOTIFY_MESSAGES_RECEIVED.channel)

        privateChannel.bind(Constants.EventsSocket.NOTIFY_MESSAGES_RECEIVED.channel,
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

        unbindChannel(privateChannel, Constants.EventsSocket.NOTIFY_MESSAGE_READED.channel)

        privateChannel.bind(Constants.EventsSocket.NOTIFY_MESSAGE_READED.channel,
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

        unbindChannel(privateChannel, Constants.EventsSocket.SEND_MESSAGES_DESTROY.channel)

        privateChannel.bind(Constants.EventsSocket.SEND_MESSAGES_DESTROY.channel,
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

        unbindChannel(globalChannel, CLIENT_CONVERSATION_NN)

        globalChannel.bind(
            CLIENT_CONVERSATION_NN,
            object : PrivateChannelEventListener {
                override fun onEvent(event: PusherEvent?) {
                    try {
                        event?.data?.let { dataEventRes ->

                            val jsonAdapter: JsonAdapter<ValidateMessageEventDTO> =
                                moshi.adapter(ValidateMessageEventDTO::class.java)

                            val dataEvent = jsonAdapter.fromJson(dataEventRes)
//
//                            val userId = repository.getUserId()

                            val messages = dataEvent?.messages?.filter {
                                it.user == userId
                            }?.filter { syncManager.existIdMessage(it.id) }

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
            Constants.EventsSocket.CANCEL_OR_REJECT_FRIENDSHIP_REQUEST.channel
        )

        privateChannel.bind(Constants.EventsSocket.CANCEL_OR_REJECT_FRIENDSHIP_REQUEST.channel,
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

        unbindChannel(privateChannel, Constants.EventsSocket.BLOCK_OR_DELETE_FRIENDSHIP.channel)

        privateChannel.bind(Constants.EventsSocket.BLOCK_OR_DELETE_FRIENDSHIP.channel,
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


    //region Metodos de Llamadas

    private fun listenUserAvailableForCall(privateChannel: PrivateChannel) {

        unbindChannel(privateChannel, Constants.EventsSocket.USER_AVAILABLE_FOR_CALL.channel)

        privateChannel.bind(Constants.EventsSocket.USER_AVAILABLE_FOR_CALL.channel,
            object : PrivateChannelEventListener {
                override fun onEvent(event: PusherEvent) {
                    try {
                        Timber.d("-- UserAvailableForCallEvent ${event.data}")
                        val jsonObject = JSONObject(event.data)
                        if (jsonObject.has("data")) {
                            val jsonObjectData = jsonObject.getJSONObject("data")
                            if (jsonObjectData.has("channel_private")) {
                                subscribeToCallChannelUserAvailableForCall(
                                    "presence-${
                                        jsonObjectData.getString(
                                            "channel_private"
                                        )
                                    }"
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Timber.e("UserAvailableForCallEvent: $e")
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

    private fun listenIncomingCall(privateChannel: PrivateChannel) {

        unbindChannel(privateChannel, Constants.EventsSocket.CALL_FRIEND.channel)

        privateChannel.bind(Constants.EventsSocket.CALL_FRIEND.channel,
            object : PrivateChannelEventListener {
                override fun onEvent(event: PusherEvent) {
                    Timber.d("CallFriendEvent SocketData: ${event.data}")
                    try {

                        val adapter: JsonAdapter<IncomingCall> =
                            moshi.adapter(IncomingCall::class.java)

                        adapter.fromJson(event.data)?.let { incomingCall ->

                            val channel = "presence-${incomingCall.data.channel}"

                            adapter.fromJson(event.data)

                            val isOnCallPref = Data.isOnCall

                            Timber.d("IsOnCall: $isOnCallPref")

                            if (isOnCallPref) {
                                syncManager.rejectCall(
                                    incomingCall.data.contactId,
                                    channel
                                )
                            } else {
                                if (context is NapoleonApplication) {
                                    val app = context
                                    if (app.isAppVisible()) {
                                        Data.isOnCall = true
                                        RxBus.publish(
                                            RxEvent.IncomingCall(
                                                channel,
                                                incomingCall.data.contactId,
                                                incomingCall.data.isVideoCall
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
            })
    }

    private fun listenRejectedCall(privateChannel: PrivateChannel) {

        unbindChannel(privateChannel, Constants.EventsSocket.REJECTED_CALL.channel)

        privateChannel.bind(Constants.EventsSocket.REJECTED_CALL.channel,
            object : PrivateChannelEventListener {
                override fun onEvent(event: PusherEvent) {
                    Timber.d("RejectedCallEvent: ${event.data}")
                    RxBus.publish(RxEvent.ContactRejectCall(event.channelName))
                }

                override fun onAuthenticationFailure(
                    message: String?,
                    e: java.lang.Exception?
                ) = Unit

                override fun onSubscriptionSucceeded(channelName: String?) = Unit
            })
    }

    private fun listenCancelCall(privateChannel: PrivateChannel) {

        unbindChannel(privateChannel, Constants.EventsSocket.CANCEL_CALL.channel)

        privateChannel.bind(Constants.EventsSocket.CANCEL_CALL.channel,
            object : PrivateChannelEventListener {
                override fun onEvent(event: PusherEvent) {
                    Timber.d("CancelCallEvent: ${event.data}, notificationId: ${NotificationService.NOTIFICATION_RINGING}")
                    val jsonObject = JSONObject(event.data)
                    if (jsonObject.has("data")) {
                        val jsonData = jsonObject.getJSONObject("data")
                        if (jsonData.has("channel_private")) {
                            val privateChannel = jsonData.getString("channel_private")
                            RxBus.publish(RxEvent.ContactCancelCall(privateChannel))
                        }
                    }
                    val intent = Intent(context, WebRTCCallService::class.java)
                    intent.action = WebRTCCallService.ACTION_CALL_END
                    context.startService(intent)
                }

                override fun onAuthenticationFailure(
                    message: String?,
                    e: java.lang.Exception?
                ) = Unit

                override fun onSubscriptionSucceeded(channelName: String?) = Unit
            })
    }
    //endregion


    private fun unbindChannel(privateChannel: PrivateChannel, channelName: String) {
        privateChannel.unbind(channelName, SubscriptionEventListener { })

    }

    //endregion

    //region Llamadas

    //region Metodos de la interfaz
    override fun getPusherChannel(channel: String): PresenceChannel? =
        pusher.getPresenceChannel(channel)


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

    override fun unSubscribeCallChannel(channelName: String) {
//        pusher.unsubscribe(channelName)
//        Timber.d("unsubscribe to channel: $channelName")
    }

    //
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


    /**
     * El channel debe tener como prefijo presence-
     */
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

    //endregion

    //region Metodos Privados
    //        override fun subscribeToCallChannelFromBackground(channel: String) {
//        Timber.d("subscribeToCallChannelFromBackground: $channel")
//        callChannel = pusher.subscribePresence(channel, object : PresenceChannelEventListener {
//            override fun onEvent(event: PusherEvent) {
//                Timber.d("event: ${event.data}")
//            }
//
//            override fun onAuthenticationFailure(message: String, e: java.lang.Exception) = Unit
//
//            override fun onSubscriptionSucceeded(channelName: String) {
//                listenCallEvents(callChannel!!)
//                Data.isOnCall = true
//
//                joinToCall(channelName)
//
//                Timber.d("Subscribe call channel to $channelName")
//            }
//
//            override fun onUsersInformationReceived(
//                channelName: String?,
//                users: MutableSet<User>?
//            ) {
//                Timber.d("onUsersInformationReceived: $channel, $users")
//            }
//
//            override fun userSubscribed(channelName: String?, user: User?) {
//                Timber.d("userSubscribed: $channel, $user")
//            }
//
//            override fun userUnsubscribed(channelName: String?, user: User?) {
//                Timber.d("userUnsubscribed: $channel, $user")
//            }
//        })
//    }
//
//

//

    //

//
//

//
//
//    private fun listenCallEvents(privateChannel: PrivateChannel) {
//        try {
//            privateChannel.bind(CALL_NN, object : PresenceChannelEventListener {
//                override fun onEvent(event: PusherEvent) {
//                    try {
//                        val eventType = event.data.toIntOrNull()
//
//                        if (eventType != null) {
//
//                            Timber.d("LLegó $CALL_NN $eventType")
//
//                            when (eventType) {
//                                CONTACT_JOIN_TO_CALL -> RxBus.publish(
//                                    RxEvent.ContactHasJoinToCall(
//                                        event.channelName
//                                    )
//                                )
//                                HANGUP_CALL -> RxBus.publish(RxEvent.ContactHasHangup(event.channelName))
//                                CONTACT_WANT_CHANGE_TO_VIDEO -> RxBus.publish(
//                                    RxEvent.ContactWantChangeToVideoCall(
//                                        event.channelName
//                                    )
//                                )
//                                CONTACT_ACCEPT_CHANGE_TO_VIDEO -> RxBus.publish(
//                                    RxEvent.ContactAcceptChangeToVideoCall(
//                                        event.channelName
//                                    )
//                                )
//                                CONTACT_TURN_OFF_CAMERA -> RxBus.publish(
//                                    RxEvent.ContactTurnOffCamera(
//                                        event.channelName
//                                    )
//                                )
//                                CONTACT_TURN_ON_CAMERA -> RxBus.publish(
//                                    RxEvent.ContactTurnOnCamera(
//                                        event.channelName
//                                    )
//                                )
//                                CONTACT_CANCEL_CHANGE_TO_VIDEO -> RxBus.publish(
//                                    RxEvent.ContactCancelChangeToVideoCall(
//                                        event.channelName
//                                    )
//                                )
//                                CONTACT_CANT_CHANGE_TO_VIDEO -> RxBus.publish(
//                                    RxEvent.ContactCantChangeToVideoCall(
//                                        event.channelName
//                                    )
//                                )
//                            }
//                        } else {
//                            val jsonData = JSONObject(event.data)
//
//                            Timber.d("LLegó $CALL_NN $jsonData")
//
//                            if (jsonData.has(TYPE)) {
//
//                                when (jsonData.getString(TYPE)) {
//                                    ICE_CANDIDATE -> RxBus.publish(
//                                        RxEvent.IceCandidateReceived(
//                                            event.channelName,
//                                            jsonData.toIceCandidate()
//                                        )
//                                    )
//                                    OFFER -> {
//                                        RxBus.publish(
//                                            RxEvent.OfferReceived(
//                                                event.channelName,
//                                                jsonData.toSessionDescription(
//                                                    SessionDescription.Type.OFFER
//                                                )
//                                            )
//                                        )
//                                    }
//                                    ANSWER -> RxBus.publish(
//                                        RxEvent.AnswerReceived(
//                                            event.channelName,
//                                            jsonData.toSessionDescription(
//                                                SessionDescription.Type.ANSWER
//                                            )
//                                        )
//                                    )
//                                }
//                            }
//                        }
//                    } catch (e: Exception) {
//                        Timber.e(e)
//                    }
//                }
//
//                override fun onAuthenticationFailure(
//                    message: String?,
//                    e: java.lang.Exception?
//                ) {
//                    Timber.d("onAuthenticationFailure, $message")
//                }
//
//                override fun onSubscriptionSucceeded(channelName: String?) {
//                    Timber.d("onSubscriptionSucceeded, $channelName")
//                }
//
//                override fun onUsersInformationReceived(
//                    channelName: String?,
//                    users: MutableSet<User>?
//                ) {
//                    Timber.d("onUsersInformationReceived, $channelName, $users")
//                }
//
//                override fun userSubscribed(channelName: String?, user: User?) {
//                    Timber.d("userSubscribed, $channelName, $user")
//                }
//
//                override fun userUnsubscribed(channelName: String?, user: User?) {
//                    Timber.d("userUnsubscribed, $channelName, $user")
//                }
//            })
//        } catch (e: Exception) {
//            Timber.e(e)
//        }
//    }
//

//
    //endregion

    //endregion

}