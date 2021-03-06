package com.naposystems.napoleonchat.webService.socket

import android.content.Context
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.pusher.client.Pusher
import com.pusher.client.channel.PresenceChannel
import com.pusher.client.channel.PrivateChannel
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class SocketService @Inject constructor(
    private val context: Context,
    private val pusher: Pusher,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val repository: IContractSocketService.Repository,
    private val cryptoMessage: CryptoMessage
) : IContractSocketService.SocketService {

//    private val moshi: Moshi by lazy {
//        Moshi.Builder().build()
//    }
//
//    private val app: NapoleonApplication by lazy {
//        context as NapoleonApplication
//    }

    private var userId = repository.getUser()

    private var privateGeneralChannelName: String

    private var privateGlobalChannelName: String

    private lateinit var generalChannel: PrivateChannel

    private lateinit var globalChannel: PrivateChannel

//    private var callChannel: PresenceChannel? = null


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

    //region Socket Mensajes
    override fun connectSocket() {

        Timber.d("Pusher: *****************")

        Timber.d("Pusher: connectSocket: State:${pusher.connection.state}")

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
        } else if (pusher.connection.state == ConnectionState.CONNECTED) {
            subscribeChannels()
        }

    }
//endregion

//region Socket Llamada

//endregion

//region Metodos Privados

    private fun subscribeChannels() {
        Timber.d("Pusher: subscribeChannel")
        try {
            sharedPreferencesManager.putString(
                Constants.SharedPreferences.PREF_SOCKET_ID,
                pusher.connection.socketId
            )

            Timber.d("Pusher: State:${pusher.connection.state}, SocketId: ${pusher.connection.socketId}")

            pusher.unsubscribe(privateGeneralChannelName)

            subscribeToPrivateGeneralChannel()

            pusher.unsubscribe(privateGlobalChannelName)

            subscribeToPrivateGlobalChannel()

        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun subscribeToPrivateGeneralChannel() {

        Timber.d("Pusher: subscribeToPrivateGeneralChannel: instance:$this")

        Timber.d(
            "Pusher: subscribeToPrivateGeneralChannel:  privateChannel: ${
                pusher.getPrivateChannel(
                    privateGeneralChannelName
                )
            }"
        )

        try {

            if (pusher.getPrivateChannel(privateGeneralChannelName) == null) {


                Timber.d(
                    "Pusher: subscribeToPrivateGeneralChannel:  IF privateChannel: ${
                        pusher.getPrivateChannel(
                            privateGeneralChannelName
                        )
                    }"
                )
                            generalChannel = pusher.subscribePrivate(
                                privateGeneralChannelName,
                                object : PrivateChannelEventListener {
//                        override fun onEvent(event: PusherEvent) {
//                            Timber.d("Subscribe general: ${event.data}")
//                        }
//
//                        override fun onAuthenticationFailure(
//                            message: String?,
//                            e: java.lang.Exception?
//                        ) {
//                            Timber.d("Pusher: subscribeToPrivateGeneralChannel: onAuthenticationFailure")
//                        }
//
//                        override fun onSubscriptionSucceeded(channelName: String?) {
//
//                            Timber.d("onSubscriptionSucceeded: $channelName")
//
//                            if (!Data.isGeneralChannelSubscribed) {
//
//                                Data.isGeneralChannelSubscribed = true
//
//                                listenOnDisconnect(generalChannel)
//
//                                listenNewMessageEvent(generalChannel)
//
//                                listenNotifyMessagesReceived(generalChannel)
//
//                                listenCancelOrRejectFriendshipRequestEvent(
//                                    generalChannel
//                                )
//
//                                listenMessagesRead(generalChannel)
//
//                                listenMessagesDestroy(generalChannel)
//
//                                listenIncomingCall(generalChannel)
//
//                                listenCallRejected(generalChannel)
//
//                                listenCancelCall(generalChannel)
//
//                                listenContactBlockOrDelete(generalChannel)
//
//                                listenUserAvailableForCall(generalChannel)
//                            }
//
//                            repository.getMyMessages(null)
//
//                            repository.verifyMessagesReceived()
//
//                            repository.verifyMessagesRead()

//                        }
//
//                    }
//
//                )
            }

        } catch (e: Exception) {

            Timber.e("Pusher:  subscribeToPrivateGeneralChannel: Exception: $e")

        }

    }

    private fun subscribeToPrivateGlobalChannel() {

        Timber.d("Pusher: subscribeToPrivateGlobalChannel instance:$this")

        Timber.d(
            "Pusher: subscribeToPrivateGlobalChannel:  privateChannel: ${
                pusher.getPrivateChannel(
                    privateGeneralChannelName
                )
            }"
        )

        try {

            if (pusher.getPrivateChannel(privateGlobalChannelName) == null) {

                Timber.d(
                    "Pusher: subscribeToPrivateGlobalChannel:  IF privateChannel: ${
                        pusher.getPrivateChannel(
                            privateGeneralChannelName
                        )
                    }"
                )

//                globalChannel = pusher.subscribePrivate(
//                    channelPrivateGlobalName,
//                    object : PrivateChannelEventListener {
//                        override fun onEvent(event: PusherEvent?) {
//                            Timber.d("Subscribe private-global")
//                        }
//
//                        override fun onAuthenticationFailure(
//                            message: String?,
//                            e: java.lang.Exception?
//                        ) {
//                            Timber.d("$message, $e")
//                        }
//
//                        override fun onSubscriptionSucceeded(channelName: String?) {
//                            Timber.d("onSubscriptionSucceeded: $channelName")
//                            listenValidateConversationEvent()
//
//                            RxBus.publish(RxEvent.CreateNotification())
//                        }
//                    }
//                )
            }

        } catch (e: Exception) {
            Timber.e("Pusher:  subscribeToPrivateGlobalChannel: Exception: $e")
        }
    }

//endregion


    override fun disconnectSocket() {
//        try {
//            if (pusher.connection.state == ConnectionState.CONNECTED ||
//                pusher.connection.state == ConnectionState.CONNECTING
//            ) {
//                pusher.disconnect()
//                Timber.d("Socket disconnected")
//            }
//        } catch (e: Exception) {
//            Timber.e(e)
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

    //    override fun subscribeToCallChannelFromBackground(channel: String) {
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
//    fun subscribeToCallChannelUserAvailableForCall(channel: String) {
//        Timber.d("subscribeToCallChannel: $channel")
//        if (pusher.getPresenceChannel(channel) == null) {
//            callChannel =
//                pusher.subscribePresence(channel, object : PresenceChannelEventListener {
//                    override fun onEvent(event: PusherEvent) {
//                        Timber.d("event: ${event.data}")
//                    }
//
//                    override fun onAuthenticationFailure(
//                        message: String,
//                        e: java.lang.Exception
//                    ) = Unit
//
//                    override fun onSubscriptionSucceeded(channelName: String) {
//                        Timber.d("onSubscriptionSucceeded: $channelName")
//                        if (pusher.getPresenceChannel(channelName) != null) {
//                            pusher.unsubscribe(channelName)
//                        }
//                    }
//
//                    override fun onUsersInformationReceived(
//                        channelName: String?,
//                        users: MutableSet<User>?
//                    ) {
//                        Timber.d("onUsersInformationReceived, $channelName, $users")
//                    }
//
//                    override fun userSubscribed(channelName: String?, user: User?) {
//                        Timber.d("userSubscribed, $channelName, $user")
//                    }
//
//                    override fun userUnsubscribed(channelName: String?, user: User?) {
//                        Timber.d("userUnsubscribed, $channelName, $user")
//                    }
//                })
//        }
//    }
//
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

//    private fun listenOnDisconnect(privateChannel: PrivateChannel) {
//        privateChannel.bind(
//            Constants.EventsSocket.DISCONNECT.channel,
//            object : PrivateChannelEventListener {
//                override fun onEvent(event: PusherEvent?) {
//                    Timber.d("Socket disconnect ${event?.data}")
//                    pusher.connect()
//                }
//
//                override fun onAuthenticationFailure(message: String?, e: java.lang.Exception?) =
//                    Unit
//
//                override fun onSubscriptionSucceeded(channelName: String?) = Unit
//            })
//    }


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

    //    private fun listenValidateConversationEvent() {
//        globalChannel.bind(
//            CLIENT_CONVERSATION_NN,
//            object : PrivateChannelEventListener {
//                override fun onEvent(event: PusherEvent?) {
//                    try {
//                        event?.data?.let { dataEventRes ->
//                            val jsonAdapter: JsonAdapter<ValidateMessageEventDTO> =
//                                moshi.adapter(ValidateMessageEventDTO::class.java)
//                            val dataEvent = jsonAdapter.fromJson(dataEventRes)
//
//                            val userId = repository.getUser()
//
////            val userId = repository.getUser
////                sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_USER_ID)
//
//                            val messages = dataEvent?.messages?.filter {
//                                it.user == userId
//                            }?.filter { repository.existIdMessage(it.id) }
//
//                            val unread = messages?.filter {
//                                it.status == Constants.MessageEventType.UNREAD.status
//                            }?.map { it.id }
//
//                            unread?.let {
//                                repository.updateMessagesStatus(
//                                    it,
//                                    Constants.MessageStatus.UNREAD.status
//                                )
//                            }
//
//                            val read = messages?.filter {
//                                it.status == Constants.MessageEventType.READ.status
//                            }?.map { it.id }
//
//                            read?.let {
//                                repository.validateMessageType(
//                                    it,
//                                    Constants.MessageStatus.READED.status
//                                )
//                            }
//                        }
//                    } catch (e: Exception) {
//                        Timber.e(e)
//                    }
//                }
//
//                override fun onSubscriptionSucceeded(channelName: String?) = Unit
//
//                override fun onAuthenticationFailure(message: String?, e: java.lang.Exception?) =
//                    Unit
//            }
//        )
//    }
//
    override fun emitToClientConversation(jsonObject: String) {
//        Timber.d("SocketService: $this")
//        try {
//            if (jsonObject.isNotEmpty())
//                globalChannel.trigger(CLIENT_CONVERSATION_NN, jsonObject)
//        } catch (e: Exception) {
//            Timber.e(e)
//        }
    }
//
//    private fun listenNewMessageEvent(privateChannel: PrivateChannel) {
//        privateChannel.bind(Constants.EventsSocket.NEW_MESSAGE.channel,
//            object : PrivateChannelEventListener {
//                override fun onEvent(event: PusherEvent?) {
//                    Timber.d("NewMessageEvent: ${event?.data}")
//                    if (app.isAppVisible()) {
//                        try {
//                            event?.data?.let { dataEventRes ->
//                                val jsonAdapterData: JsonAdapter<NewMessageEventRes> =
//                                    moshi.adapter(NewMessageEventRes::class.java)
//                                val dataEvent = jsonAdapterData.fromJson(dataEventRes)
//
//                                dataEvent?.data?.let { newMessageDataEventRes ->
//
//                                    val message =
//                                        ValidateMessage(
//                                            id = newMessageDataEventRes.messageId,
//                                            user = newMessageDataEventRes.contactId,
//                                            status = Constants.MessageEventType.UNREAD.status
//                                        )
//
//                                    repository.insertNewMessage(newMessageDataEventRes)
//
//                                    val data: String = if (BuildConfig.ENCRYPT_API) {
//                                        cryptoMessage.decryptMessageBody(newMessageDataEventRes.message)
//                                    } else {
//                                        newMessageDataEventRes.message
//                                    }
//
//                                    val jsonAdapterMessage: JsonAdapter<NewMessageEventMessageRes> =
//                                        moshi.adapter(NewMessageEventMessageRes::class.java)
//
//                                    jsonAdapterMessage.fromJson(data)
//                                        ?.let { newMessageEventMessageRes ->
//
//                                            if (newMessageEventMessageRes.numberAttachments > 0) {
//                                                if ((availableToReceived(newMessageEventMessageRes.attachments) && Data.contactId == newMessageEventMessageRes.userAddressee) ||
//                                                    Data.contactId == 0
//                                                ) {
//                                                    repository.notifyMessageReceived(message.id)
//                                                    emmitReceived(message)
//                                                }
//                                            } else if (Data.contactId != newMessageEventMessageRes.userAddressee) {
//                                                repository.notifyMessageReceived(message.id)
//                                                emmitReceived(message)
//                                            }
//                                        }
//                                }
//                            }
//
//                        } catch (e: Exception) {
//                            Timber.e(e)
//                        }
//                    }
//                }
//
//                override fun onAuthenticationFailure(
//                    message: String?,
//                    e: java.lang.Exception?
//                ) = Unit
//
//                override fun onSubscriptionSucceeded(channelName: String?) = Unit
//            })
//    }
//
//    private fun availableToReceived(attachments: List<NewMessageEventAttachmentRes>): Boolean {
//
//        var attachment: NewMessageEventAttachmentRes? = attachments.firstOrNull() {
//            it.type == Constants.AttachmentType.IMAGE.type ||
//                    it.type == Constants.AttachmentType.AUDIO.type ||
//                    it.type == Constants.AttachmentType.VIDEO.type ||
//                    it.type == Constants.AttachmentType.DOCUMENT.type
//        }
//
//        return attachment != null
//
//    }
//
//    private fun emmitReceived(messages: ValidateMessage) {
//
//        val validateMessage =
//            ValidateMessageEventDTO(arrayListOf(messages))
//
//        val jsonAdapterValidate =
//            moshi.adapter(ValidateMessageEventDTO::class.java)
//
//        val json =
//            jsonAdapterValidate.toJson(validateMessage)
//
//        globalChannel.trigger(
//            CLIENT_CONVERSATION_NN,
//            json.toString()
//        )
//    }
//
//    private fun listenNotifyMessagesReceived(privateChannel: PrivateChannel) {
//        privateChannel.bind(Constants.EventsSocket.NOTIFY_MESSAGES_RECEIVED.channel,
//            object : PrivateChannelEventListener {
//                override fun onEvent(event: PusherEvent?) {
//                    Timber.d("NotifyMessagesReceived: ${event?.data}")
//                    event?.data?.let {
//                        val jsonAdapter: JsonAdapter<MessagesReceivedDTO> =
//                            moshi.adapter(MessagesReceivedDTO::class.java)
//
//                        val dataEvent = jsonAdapter.fromJson(it)
//
//                        dataEvent?.let { messagesReceivedDTO ->
//
//                            Timber.d(messagesReceivedDTO.data.messageIds.toString())
//
//                            repository.updateMessagesStatus(
//                                messagesReceivedDTO.data.messageIds,
//                                Constants.MessageStatus.UNREAD.status
//                            )
//                        }
//                    }
//                }
//
//                override fun onAuthenticationFailure(
//                    message: String?,
//                    e: java.lang.Exception?
//                ) = Unit
//
//                override fun onSubscriptionSucceeded(channelName: String?) = Unit
//            })
//    }
//
//    private fun listenCancelOrRejectFriendshipRequestEvent(privateChannel: PrivateChannel) {
//        privateChannel.bind(Constants.EventsSocket.CANCEL_OR_REJECT_FRIENDSHIP_REQUEST.channel,
//            object : PrivateChannelEventListener {
//                override fun onEvent(event: PusherEvent?) {
//                    RxBus.publish(RxEvent.CancelOrRejectFriendshipRequestEvent())
//                }
//
//                override fun onSubscriptionSucceeded(channelName: String?) = Unit
//
//                override fun onAuthenticationFailure(
//                    message: String?,
//                    e: java.lang.Exception?
//                ) =
//                    Unit
//            }
//        )
//    }
//
//    private fun listenMessagesRead(privateChannel: PrivateChannel) {
//        privateChannel.bind(Constants.EventsSocket.NOTIFY_MESSAGE_READED.channel,
//            object : PrivateChannelEventListener {
//                override fun onEvent(event: PusherEvent?) {
//                    Timber.d("NotifyMessageReaded: ${event?.data}")
//
//                    event?.data?.let {
//                        val jsonAdapter: JsonAdapter<MessagesReadedDTO> =
//                            moshi.adapter(MessagesReadedDTO::class.java)
//
//                        val dataEvent = jsonAdapter.fromJson(it)
//
//                        dataEvent?.let { messagesReadedDTO ->
//
//                            Timber.d(messagesReadedDTO.data.messageIds.toString())
//
//                            repository.updateMessagesStatus(
//                                messagesReadedDTO.data.messageIds,
//                                Constants.MessageStatus.READED.status
//                            )
//                        }
//                    }
//
//                    repository.verifyMessagesRead()
//                }
//
//                override fun onAuthenticationFailure(
//                    message: String?,
//                    e: java.lang.Exception?
//                ) = Unit
//
//                override fun onSubscriptionSucceeded(channelName: String?) = Unit
//            })
//    }
//
//    private fun listenMessagesDestroy(privateChannel: PrivateChannel) {
//        privateChannel.bind(Constants.EventsSocket.SEND_MESSAGES_DESTROY.channel,
//            object : PrivateChannelEventListener {
//                override fun onEvent(event: PusherEvent?) {
//                    Timber.d("SendMessagesDestroyEvent: ${event?.data}")
//                    repository.getDeletedMessages()
//                }
//
//                override fun onAuthenticationFailure(
//                    message: String?,
//                    e: java.lang.Exception?
//                ) = Unit
//
//                override fun onSubscriptionSucceeded(channelName: String?) = Unit
//            })
//    }
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
//    private fun listenIncomingCall(privateChannel: PrivateChannel) {
//        privateChannel.bind(Constants.EventsSocket.CALL_FRIEND.channel,
//            object : PrivateChannelEventListener {
//                override fun onEvent(event: PusherEvent) {
//                    Timber.d("CallFriendEvent SocketData: ${event.data}")
//                    try {
//
//                        val adapter: JsonAdapter<IncomingCall> =
//                            moshi.adapter(IncomingCall::class.java)
//
//                        adapter.fromJson(event.data)?.let { incomingCall ->
//
//                            val channel = "presence-${incomingCall.data.channel}"
//
//                            adapter.fromJson(event.data)
//
//                            val isOnCallPref = Data.isOnCall
//
//                            Timber.d("IsOnCall: $isOnCallPref")
//
//                            if (isOnCallPref) {
//                                repository.rejectCall(
//                                    incomingCall.data.contactId,
//                                    channel
//                                )
//                            } else {
//                                if (context is NapoleonApplication) {
//                                    val app = context
//                                    if (app.isAppVisible()) {
//                                        Data.isOnCall = true
//                                        RxBus.publish(
//                                            RxEvent.IncomingCall(
//                                                channel,
//                                                incomingCall.data.contactId,
//                                                incomingCall.data.isVideoCall
//                                            )
//                                        )
//                                    }
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
//                ) = Unit
//
//                override fun onSubscriptionSucceeded(channelName: String?) = Unit
//            })
//    }
//
//    private fun listenCallRejected(privateChannel: PrivateChannel) {
//        privateChannel.bind(Constants.EventsSocket.REJECTED_CALL.channel,
//            object : PrivateChannelEventListener {
//                override fun onEvent(event: PusherEvent) {
//                    Timber.d("RejectedCallEvent: ${event.data}")
//                    RxBus.publish(RxEvent.ContactRejectCall(event.channelName))
//                }
//
//                override fun onAuthenticationFailure(
//                    message: String?,
//                    e: java.lang.Exception?
//                ) = Unit
//
//                override fun onSubscriptionSucceeded(channelName: String?) = Unit
//            })
//    }
//
//    private fun listenCancelCall(privateChannel: PrivateChannel) {
//        privateChannel.bind(Constants.EventsSocket.CANCEL_CALL.channel,
//            object : PrivateChannelEventListener {
//                override fun onEvent(event: PusherEvent) {
//                    Timber.d("CancelCallEvent: ${event.data}, notificationId: ${NotificationUtils.NOTIFICATION_RINGING}")
//                    val jsonObject = JSONObject(event.data)
//                    if (jsonObject.has("data")) {
//                        val jsonData = jsonObject.getJSONObject("data")
//                        if (jsonData.has("channel_private")) {
//                            val privateChannel = jsonData.getString("channel_private")
//                            RxBus.publish(RxEvent.ContactCancelCall(privateChannel))
//                        }
//                    }
//                    val intent = Intent(context, WebRTCCallService::class.java)
//                    intent.action = WebRTCCallService.ACTION_CALL_END
//                    context.startService(intent)
//                }
//
//                override fun onAuthenticationFailure(
//                    message: String?,
//                    e: java.lang.Exception?
//                ) = Unit
//
//                override fun onSubscriptionSucceeded(channelName: String?) = Unit
//            })
//    }

    override fun getPusherChannel(channel: String): PresenceChannel? =
        pusher.getPresenceChannel(channel)

//    private fun listenContactBlockOrDelete(privateChannel: PrivateChannel) {
//        privateChannel.bind(Constants.EventsSocket.BLOCK_OR_DELETE_FRIENDSHIP.channel,
//            object : PrivateChannelEventListener {
//                override fun onEvent(event: PusherEvent) {
//                    Timber.d("-- BlockOrDeleteFrienshipEvent ${event.data}")
//                    val jsonObject = JSONObject(event.data)
//                    if (jsonObject.has("data")) {
//                        jsonObject.getJSONObject("data").let { jsonData ->
//                            if (jsonData.has("contact_id")) {
//                                jsonData.getInt("contact_id").let { contactId ->
//                                    repository.deleteContact(contactId)
//                                    RxBus.publish(
//                                        RxEvent.ContactBlockOrDelete(
//                                            jsonData.getInt("contact_id")
//                                        )
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//
//                override fun onAuthenticationFailure(
//                    message: String?,
//                    e: java.lang.Exception?
//                ) =
//                    Unit
//
//                override fun onSubscriptionSucceeded(channelName: String?) = Unit
//
//            })
//    }
//
//    private fun listenUserAvailableForCall(privateChannel: PrivateChannel) {
//        privateChannel.bind(Constants.EventsSocket.USER_AVAILABLE_FOR_CALL.channel,
//            object : PrivateChannelEventListener {
//                override fun onEvent(event: PusherEvent) {
//                    try {
//                        Timber.d("-- UserAvailableForCallEvent ${event.data}")
//                        val jsonObject = JSONObject(event.data)
//                        if (jsonObject.has("data")) {
//                            val jsonObjectData = jsonObject.getJSONObject("data")
//                            if (jsonObjectData.has("channel_private")) {
//                                subscribeToCallChannelUserAvailableForCall(
//                                    "presence-${
//                                        jsonObjectData.getString(
//                                            "channel_private"
//                                        )
//                                    }"
//                                )
//                            }
//                        }
//                    } catch (e: Exception) {
//                        Timber.e("UserAvailableForCallEvent: $e")
//                    }
//                }
//
//                override fun onAuthenticationFailure(
//                    message: String?,
//                    e: java.lang.Exception?
//                ) =
//                    Unit
//
//                override fun onSubscriptionSucceeded(channelName: String?) = Unit
//
//            })
//    }

}