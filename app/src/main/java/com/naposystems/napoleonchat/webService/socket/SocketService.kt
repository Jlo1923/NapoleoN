package com.naposystems.napoleonchat.webService.socket

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.dto.newMessageEvent.NewMessageEventRes
import com.naposystems.napoleonchat.model.conversationCall.IncomingCall
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.webRTCCall.WebRTCCallService
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Data
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.adapters.toIceCandidate
import com.naposystems.napoleonchat.utility.adapters.toSessionDescription
import com.naposystems.napoleonchat.utility.notificationUtils.NotificationUtils
import com.pusher.client.Pusher
import com.pusher.client.channel.Channel
import com.pusher.client.channel.PrivateChannel
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.channel.PusherEvent
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.json.JSONObject
import org.webrtc.SessionDescription
import timber.log.Timber
import javax.inject.Inject

class SocketService @Inject constructor(
    private val context: Context,
    private val pusher: Pusher,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val repository: IContractSocketService.Repository
) : IContractSocketService.SocketService {

    private val firebaseId by lazy {
        sharedPreferencesManager.getString(Constants.SharedPreferences.PREF_FIREBASE_ID, "")
    }

    private lateinit var generalChannel: PrivateChannel
    private var callChannel: PrivateChannel? = null

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

    override fun initSocket() {
        try {
            Timber.d("init")

            connectToSocket()

        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun disconnectSocket() {
        try {
            pusher.disconnect()
            Timber.d("Socket disconnected")
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun subscribe(jsonObject: String) {
        connectToSocket()
        Timber.d("Subscribe to $jsonObject")
    }

    override fun subscribeToCallChannel(channel: String, isActionAnswer: Boolean) {
        Timber.d("subscribeToCallChannel: $channel")
        if (pusher.getPrivateChannel(channel) == null) {
            callChannel = pusher.subscribePrivate(channel, object : PrivateChannelEventListener {
                override fun onEvent(event: PusherEvent) {
                    Timber.d("event: ${event.data}")
                }

                override fun onAuthenticationFailure(message: String, e: java.lang.Exception) {

                }

                override fun onSubscriptionSucceeded(channelName: String) {
                    listenCallEvents(callChannel!!)
                    Data.isOnCall = true

                    Timber.d("Subscribe call channel to $channelName")

                    if (isActionAnswer) {
                        joinToCall(channel)
                    }
                }
            })
        }
    }

    override fun subscribeToCallChannelFromBackground(channel: String) {
        Timber.d("subscribeToCallChannelFromBackground: $channel")
        callChannel = pusher.subscribePrivate(channel, object : PrivateChannelEventListener {
            override fun onEvent(event: PusherEvent) {
                Timber.d("event: ${event.data}")
            }

            override fun onAuthenticationFailure(message: String, e: java.lang.Exception) {

            }

            override fun onSubscriptionSucceeded(channelName: String) {
                listenCallEvents(callChannel!!)
                Data.isOnCall = true

                joinToCall(channelName)

                Timber.d("Subscribe call channel to $channelName")
            }
        })
    }

    override fun joinToCall(channel: String) {
        emitToCall(channel, CONTACT_JOIN_TO_CALL)
    }

    override fun unSubscribeCallChannel(channelName: String) {
        pusher.unsubscribe(channelName)
        Timber.d("unsubscribe to channel: $channelName")
    }

    override fun emitToCall(channel: String, jsonObject: JSONObject) {
        callChannel?.trigger(CALL_NN, jsonObject.toString())

        Timber.d("Emit to Call $jsonObject")
    }

    override fun emitToCall(channel: String, eventType: Int) {
        try {
            callChannel?.trigger(CALL_NN, eventType.toString())

            Timber.d("Emit to Call $eventType")
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun listenOnDisconnect(privateChannel: PrivateChannel) {
        privateChannel.bind("disconnect", object : PrivateChannelEventListener {
            override fun onEvent(event: PusherEvent?) {
                Timber.d("Socket disconnect ${event?.data}")
                pusher.connect()
            }

            override fun onAuthenticationFailure(message: String?, e: java.lang.Exception?) {

            }

            override fun onSubscriptionSucceeded(channelName: String?) {

            }
        })
    }

    private fun connectToSocket() {
        Timber.d("connectToSocket")
        pusher.connect(object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange?) {
                when (change?.currentState) {
                    ConnectionState.CONNECTED -> {
                        try {
                            sharedPreferencesManager.putString(
                                Constants.SharedPreferences.PREF_SOCKET_ID,
                                pusher.connection.socketId
                            )
                            Timber.d("Conectó al socket ${pusher.connection.socketId}")

                            subscribeToGeneralChannel()
                        } catch (e: Exception) {
                            Timber.e(e)
                        }
                    }
                    ConnectionState.CONNECTING -> Timber.d("Socket: ConnectionState.CONNECTING")
                    ConnectionState.DISCONNECTED -> Timber.d("Socket: ConnectionState.DISCONNECTED")
                    ConnectionState.DISCONNECTING -> Timber.d("Socket: ConnectionState.DISCONNECTING")
                    ConnectionState.RECONNECTING -> Timber.d("Socket: ConnectionState.RECONNECTING")
                    else -> Timber.d("Socket Error")
                }
            }

            override fun onError(message: String?, code: String?, e: java.lang.Exception?) {
                Timber.e("Pusher onError $message, code: $code")
                pusher.connect()
            }
        })
    }

    private fun subscribeToGeneralChannel() {
        try {
            val userId = sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_USER_ID)

            if (userId != 0) {

                val channelName =
                    "private-general.${userId}"

                generalChannel =
                    pusher.subscribePrivate(channelName, object : PrivateChannelEventListener {
                        override fun onEvent(event: PusherEvent) {
                            Timber.d("Subscribe general: ${event.data}")
                        }

                        override fun onAuthenticationFailure(
                            message: String?,
                            e: java.lang.Exception?
                        ) {
                            Timber.d("$message, $e")
                        }

                        override fun onSubscriptionSucceeded(channelName: String?) {
                            Timber.d("onSubscriptionSucceeded: $channelName")

                            listenOnDisconnect(generalChannel)

                            listenNewMessageEvent(generalChannel)

                            listenNotifyMessagesReceived(generalChannel)

                            listenCancelOrRejectFriendshipRequestEvent(generalChannel)

                            listenMessagesRead(generalChannel)

                            listenMessagesDestroy(generalChannel)

                            listenIncomingCall(generalChannel)

                            listenCallRejected(generalChannel)

                            listenCancelCall(generalChannel)

                            repository.getMyMessages(null)
                        }
                    })
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun listenNewMessageEvent(privateChannel: PrivateChannel) {
        privateChannel.bind("App\\Events\\NewMessageEvent", object : PrivateChannelEventListener {
            override fun onEvent(event: PusherEvent?) {
//                Timber.d("NewMessageEvent: ${event?.data}")
                try {
                    event?.data?.let { dataEventRes ->
                        val moshi = Moshi.Builder().build()
                        val jsonAdapter: JsonAdapter<NewMessageEventRes> =
                            moshi.adapter(NewMessageEventRes::class.java)
                        val dataEvent = jsonAdapter.fromJson(dataEventRes)

                        dataEvent?.data?.let { newMessageDataEventRes ->
                            Timber.d("NewMessageEvent: ${newMessageDataEventRes.contactId}")
                            repository.getMyMessages(newMessageDataEventRes.contactId)
                        }
                    }

                } catch (e: Exception) {
                    Timber.e(e)
                }
            }

            override fun onAuthenticationFailure(message: String?, e: java.lang.Exception?) {

            }

            override fun onSubscriptionSucceeded(channelName: String?) {

            }
        })
    }

    private fun listenNotifyMessagesReceived(privateChannel: PrivateChannel) {
        privateChannel.bind(
            "App\\Events\\NotifyMessagesReceived",
            object : PrivateChannelEventListener {
                override fun onEvent(event: PusherEvent?) {
                    Timber.d("NotifyMessagesReceived: ${event?.data}")
                    repository.verifyMessagesReceived()
                }

                override fun onAuthenticationFailure(message: String?, e: java.lang.Exception?) {

                }

                override fun onSubscriptionSucceeded(channelName: String?) {

                }
            })
    }

    private fun listenCancelOrRejectFriendshipRequestEvent(privateChannel: PrivateChannel) {
        privateChannel.bind(
            "App\\Events\\CancelOrRejectFriendshipRequestEvent",
            object : PrivateChannelEventListener {
                override fun onEvent(event: PusherEvent?) {
                    RxBus.publish(RxEvent.CancelOrRejectFriendshipRequestEvent())
                }

                override fun onSubscriptionSucceeded(channelName: String?) = Unit

                override fun onAuthenticationFailure(message: String?, e: java.lang.Exception?) =
                    Unit
            }
        )
    }

    private fun listenMessagesRead(privateChannel: PrivateChannel) {
        privateChannel.bind(
            "App\\Events\\NotifyMessageReaded",
            object : PrivateChannelEventListener {
                override fun onEvent(event: PusherEvent?) {
                    Timber.d("NotifyMessageReaded: ${event?.data}")
                    repository.verifyMessagesRead()
                }

                override fun onAuthenticationFailure(message: String?, e: java.lang.Exception?) {

                }

                override fun onSubscriptionSucceeded(channelName: String?) {

                }
            })
    }

    private fun listenMessagesDestroy(privateChannel: PrivateChannel) {
        privateChannel.bind(
            "App\\Events\\SendMessagesDestroyEvent",
            object : PrivateChannelEventListener {
                override fun onEvent(event: PusherEvent?) {
                    Timber.d("SendMessagesDestroyEvent: ${event?.data}")
                    repository.getDeletedMessages()
                }

                override fun onAuthenticationFailure(message: String?, e: java.lang.Exception?) {

                }

                override fun onSubscriptionSucceeded(channelName: String?) {

                }
            })
    }

    private fun listenCallEvents(privateChannel: PrivateChannel) {
        privateChannel.bind(CALL_NN, object : PrivateChannelEventListener {
            override fun onEvent(event: PusherEvent) {
                try {
                    val eventType = event.data.toIntOrNull()

                    if (eventType != null) {
                        Timber.d("LLegó $CALL_NN $eventType")

                        when (eventType) {
                            CONTACT_JOIN_TO_CALL -> RxBus.publish(
                                RxEvent.ContactHasJoinToCall(
                                    event.channelName
                                )
                            )
                            HANGUP_CALL -> RxBus.publish(RxEvent.ContactHasHangup(event.channelName))
                            CONTACT_WANT_CHANGE_TO_VIDEO -> RxBus.publish(
                                RxEvent.ContactWantChangeToVideoCall(
                                    event.channelName
                                )
                            )
                            CONTACT_ACCEPT_CHANGE_TO_VIDEO -> RxBus.publish(
                                RxEvent.ContactAcceptChangeToVideoCall(
                                    event.channelName
                                )
                            )
                            CONTACT_TURN_OFF_CAMERA -> RxBus.publish(
                                RxEvent.ContactTurnOffCamera(
                                    event.channelName
                                )
                            )
                            CONTACT_TURN_ON_CAMERA -> RxBus.publish(
                                RxEvent.ContactTurnOnCamera(
                                    event.channelName
                                )
                            )
                            CONTACT_CANCEL_CHANGE_TO_VIDEO -> RxBus.publish(
                                RxEvent.ContactCancelChangeToVideoCall(
                                    event.channelName
                                )
                            )
                            CONTACT_CANT_CHANGE_TO_VIDEO -> RxBus.publish(
                                RxEvent.ContactCantChangeToVideoCall(
                                    event.channelName
                                )
                            )
                        }
                    } else {
                        val jsonData = JSONObject(event.data)

                        Timber.d("LLegó $CALL_NN $jsonData")

                        if (jsonData.has(TYPE)) {

                            when (jsonData.getString(TYPE)) {
                                ICE_CANDIDATE -> RxBus.publish(
                                    RxEvent.IceCandidateReceived(
                                        event.channelName,
                                        jsonData.toIceCandidate()
                                    )
                                )
                                OFFER -> {
                                    RxBus.publish(
                                        RxEvent.OfferReceived(
                                            event.channelName,
                                            jsonData.toSessionDescription(
                                                SessionDescription.Type.OFFER
                                            )
                                        )
                                    )
                                }
                                ANSWER -> RxBus.publish(
                                    RxEvent.AnswerReceived(
                                        event.channelName,
                                        jsonData.toSessionDescription(
                                            SessionDescription.Type.ANSWER
                                        )
                                    )
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }

            override fun onAuthenticationFailure(message: String?, e: java.lang.Exception?) {

            }

            override fun onSubscriptionSucceeded(channelName: String?) {

            }
        })
    }

    private fun listenIncomingCall(privateChannel: PrivateChannel) {
        privateChannel.bind("App\\Events\\CallFriendEvent", object : PrivateChannelEventListener {
            override fun onEvent(event: PusherEvent) {
                Timber.d("CallFriendEvent SocketData: ${event.data}")
                try {
                    val moshi = Moshi.Builder().build()

                    val adapter: JsonAdapter<IncomingCall> = moshi.adapter(IncomingCall::class.java)

                    adapter.fromJson(event.data)?.let { incomingCall ->

                        val channel = "private-${incomingCall.data.channel}"

                        adapter.fromJson(event.data)

                        val isOnCallPref = Data.isOnCall

                        Timber.d("IsOnCall: $isOnCallPref")

                        if (isOnCallPref) {
                            repository.rejectCall(
                                incomingCall.data.contactId,
                                channel
                            )
                        } else {
                            if (context is NapoleonApplication) {
                                val app = context as NapoleonApplication
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

            override fun onAuthenticationFailure(message: String?, e: java.lang.Exception?) {

            }

            override fun onSubscriptionSucceeded(channelName: String?) {

            }
        })
    }

    private fun listenCallRejected(privateChannel: PrivateChannel) {
        privateChannel.bind("App\\Events\\RejectedCallEvent", object : PrivateChannelEventListener {
            override fun onEvent(event: PusherEvent) {
                Timber.d("RejectedCallEvent: ${event.data}")
                RxBus.publish(RxEvent.ContactRejectCall(event.channelName))
            }

            override fun onAuthenticationFailure(message: String?, e: java.lang.Exception?) {

            }

            override fun onSubscriptionSucceeded(channelName: String?) {

            }
        })
    }

    private fun listenCancelCall(privateChannel: PrivateChannel) {
        privateChannel.bind("App\\Events\\CancelCallEvent", object : PrivateChannelEventListener {
            override fun onEvent(event: PusherEvent) {
                Timber.d("CancelCallEvent: ${event.data}, notificationId: ${NotificationUtils.NOTIFICATION_RINGING}")
                val jsonObject = JSONObject(event.data)
                if (jsonObject.has("data")) {
                    val jsonData = jsonObject.getJSONObject("data")
                    if (jsonData.has("channel_private")) {
                        val privateChannel = jsonData.getString("channel_private")
                        RxBus.publish(RxEvent.ContactCancelCall(privateChannel))
                    }
                }
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(NotificationUtils.NOTIFICATION_RINGING)
                val intent = Intent(context, WebRTCCallService::class.java)
                context.stopService(intent)
            }

            override fun onAuthenticationFailure(message: String?, e: java.lang.Exception?) {}

            override fun onSubscriptionSucceeded(channelName: String?) {}
        })
    }

    override fun getPusherChannel(channel: String): PrivateChannel? =
        pusher.getPrivateChannel(channel)
}