package com.naposystems.pepito.webService.socket

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.naposystems.pepito.firebase.TestService
import com.naposystems.pepito.model.conversationCall.ConversationCall
import com.naposystems.pepito.reactive.RxBus
import com.naposystems.pepito.reactive.RxEvent
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Constants.SharedPreferences.PREF_PENDING_CALL
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.utility.adapters.toConversationCallModel
import com.naposystems.pepito.utility.adapters.toIceCandidate
import com.naposystems.pepito.utility.adapters.toSessionDescription
import io.socket.client.Ack
import io.socket.client.Socket
import org.json.JSONObject
import org.webrtc.SessionDescription
import timber.log.Timber
import javax.inject.Inject

class SocketService @Inject constructor(
    private val context: Context,
    private val socket: Socket,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val repository: IContractSocketService.Repository
) : IContractSocketService.SocketService {

    companion object {
        const val CALL_NN = "client-callNN"
        const val CONTACT_JOIN_TO_CALL = 1
        const val HANGUP_CALL = 2
        const val CONTACT_WANT_CHANGE_TO_VIDEO = 3
        const val CONTACT_ACCEPT_CHANGE_TO_VIDEO = 4
        const val CONTACT_TURN_OFF_CAMERA = 5
        const val CONTACT_TURN_ON_CAMERA = 6
        const val TYPE = "type"
        const val ICE_CANDIDATE = "candidate"
        const val OFFER = "offer"
        const val ANSWER = "answer"
    }

    override fun initSocket() {
        try {
            Timber.d("init")

            connectToSocket()

            listenOnDisconnect()

            listenNewMessageEvent()

            listenNotifyMessagesReceived()

            listenMessagesRead()

            listenMessagesDestroy()

            listenCallEvents()

            listenIncomingCall()

            listenCallRejected()

        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun subscribe(jsonObject: JSONObject) {
        socket.emit("subscribe", jsonObject)
        Timber.d("Subscribe to $jsonObject")
    }

    override fun subscribeToCallChannel(
        channel: String,
        jsonObject: JSONObject
    ) {
        socket.emit("subscribe", jsonObject, Ack {
            Timber.e("error al suscribirse $it")
        })

        sharedPreferencesManager.putBoolean(Constants.SharedPreferences.PREF_IS_ON_CALL, true)

        Timber.d("Subscribe to $jsonObject")
    }

    override fun joinToCall(channel: String) {
        emitToCall(channel, CONTACT_JOIN_TO_CALL)
    }

    override fun unSubscribe(jsonObject: JSONObject, channelName: String) {
        socket.off(channelName)
        socket.emit("unsubscribe", jsonObject)
        Timber.d("unsubscribe to channel: $channelName")
    }

    override fun emitToCall(channel: String, jsonObject: JSONObject) {
        val finalJSONObject = JSONObject()
        finalJSONObject.put("data", jsonObject)
        finalJSONObject.put("channel", channel)
        finalJSONObject.put("event", CALL_NN)

        socket.emit("client event", finalJSONObject).on(Socket.EVENT_ERROR) {
            Timber.e("error al emitir a llamada")
        }
        Timber.d("Emit to Call $jsonObject")
    }

    override fun emitToCall(channel: String, eventType: Int) {
        val finalJSONObject = JSONObject()
        finalJSONObject.put("data", eventType)
        finalJSONObject.put("channel", channel)
        finalJSONObject.put("event", CALL_NN)
        socket.emit("client event", finalJSONObject).on(Socket.EVENT_ERROR) {
            Timber.e("error al emitir a llamada")
        }
        Timber.d("Emit to Call $eventType")
    }

    private fun listenOnDisconnect() {
        socket.on("disconnect") { reason ->
            Timber.d("Socket disconnect $reason")
            connectToSocket()
        }
    }

    private fun connectToSocket() {
        socket.connect().on(Socket.EVENT_CONNECT) {
            sharedPreferencesManager.putString(
                Constants.SharedPreferences.PREF_SOCKET_ID,
                socket.id()
            )
            Timber.d("Conectó al socket ${socket.id()}")

            val jsonStringPendingCall = sharedPreferencesManager.getString(PREF_PENDING_CALL, "")

            Timber.d("pending $jsonStringPendingCall")
            if (jsonStringPendingCall.isNotEmpty()) {
                val jsonObject = JSONObject(jsonStringPendingCall)

                validateCallData(jsonObject.toConversationCallModel(), true)
            }

        }.on(Socket.EVENT_CONNECT_ERROR) {
            Timber.e("No conectó al socket $it")
        }.on(Socket.EVENT_ERROR) {
            Timber.e("Error de mierda al conectar al puto socket ${it[0]}")
        }
    }

    private fun listenNewMessageEvent() {
        socket.on("App\\Events\\NewMessageEvent") {
            Timber.d("NewMessageEvent")
            repository.getMyMessages()
        }
    }

    private fun listenNotifyMessagesReceived() {
        socket.on("App\\Events\\NotifyMessagesReceived") {
            Timber.d("NotifyMessagesReceived")
            repository.verifyMessagesReceived()
        }
    }

    private fun listenMessagesRead() {
        socket.on("App\\Events\\NotifyMessageReaded") {
            Timber.d("NotifyMessageReaded")
            repository.verifyMessagesRead()
        }
    }

    private fun listenMessagesDestroy() {
        socket.on("App\\Events\\SendMessagesDestroyEvent") {
            Timber.d("SendMessagesDestroyEvent")
            repository.getDeletedMessages()
        }
    }

    private fun listenCallEvents() {
        socket.on(CALL_NN) { socketData ->
            if (socketData.size >= 2) {
                val channel: String = socketData[0].toString()
                when (val data: Any = socketData[1]) {
                    is Int -> {
                        val eventType: Int = data

                        Timber.d("LLegó $CALL_NN $eventType")

                        when (eventType) {
                            CONTACT_JOIN_TO_CALL -> RxBus.publish(
                                RxEvent.ContactHasJoinToCall(
                                    channel
                                )
                            )
                            HANGUP_CALL -> RxBus.publish(RxEvent.ContactHasHangup(channel))
                            CONTACT_WANT_CHANGE_TO_VIDEO -> RxBus.publish(
                                RxEvent.ContactWantChangeToVideoCall(
                                    channel
                                )
                            )
                            CONTACT_ACCEPT_CHANGE_TO_VIDEO -> RxBus.publish(
                                RxEvent.ContactAcceptChangeToVideoCall(
                                    channel
                                )
                            )
                            CONTACT_TURN_OFF_CAMERA -> RxBus.publish(
                                RxEvent.ContactTurnOffCamera(
                                    channel
                                )
                            )
                            CONTACT_TURN_ON_CAMERA -> RxBus.publish(
                                RxEvent.ContactTurnOnCamera(
                                    channel
                                )
                            )
                        }
                    }
                    is JSONObject -> {
                        val jsonData: JSONObject = data

                        Timber.d("LLegó $CALL_NN $jsonData")

                        if (jsonData.has(TYPE)) {

                            when (jsonData.getString(TYPE)) {
                                ICE_CANDIDATE -> RxBus.publish(
                                    RxEvent.IceCandidateReceived(
                                        channel,
                                        jsonData.toIceCandidate()
                                    )
                                )
                                OFFER -> {
                                    RxBus.publish(
                                        RxEvent.OfferReceived(
                                            channel,
                                            jsonData.toSessionDescription(
                                                SessionDescription.Type.OFFER
                                            )
                                        )
                                    )
                                }
                                ANSWER -> RxBus.publish(
                                    RxEvent.AnswerReceived(
                                        channel,
                                        jsonData.toSessionDescription(
                                            SessionDescription.Type.ANSWER
                                        )
                                    )
                                )
                            }
                        }

                    }
                }
            }
        }
    }

    private fun listenIncomingCall() {
        socket.on("App\\Events\\CallFriendEvent") { data ->
            Timber.d("CallFriendEvent")
            if (data.size >= 2) {
                val isOnCallPref = sharedPreferencesManager.getBoolean(
                    Constants.SharedPreferences.PREF_IS_ON_CALL,
                    false
                )

                val jsonObject = data[1] as JSONObject

                Timber.d(jsonObject.toString())

                if (jsonObject.has("data")) {

                    val jsonData = jsonObject.getJSONObject("data")

                    val conversationCall = jsonData.toConversationCallModel()

                    if (isOnCallPref) {
                        repository.rejectCall(conversationCall.contactId, conversationCall.channel)
                    } else {
                        validateCallData(conversationCall)
                    }
                }
            }
        }
    }

    private fun listenCallRejected() {
        socket.on("App\\Events\\RejectedCallEvent") {
            Timber.d("RejectedCallEvent: ${it[0]}")
            RxBus.publish(RxEvent.ContactRejectCall(it[0].toString()))
        }
    }

    private fun validateCallData(
        conversationCall: ConversationCall, isPendingCall: Boolean = false
    ) {
        if (conversationCall.channel.isNotEmpty() && conversationCall.contactId > 0) {

            if (isPendingCall) {
                startService(
                    conversationCall.channel,
                    conversationCall.contactId,
                    conversationCall.isVideoCall
                )
                sharedPreferencesManager.putString(PREF_PENDING_CALL, "")
            } else {
                RxBus.publish(
                    RxEvent.IncomingCall(
                        conversationCall.channel,
                        conversationCall.contactId,
                        conversationCall.isVideoCall
                    )
                )
            }

        }
    }

    private fun startService(channel: String, contactId: Int, isVideoCall: Boolean) {
        val service = Intent(context, TestService::class.java)

        val bundle = Bundle()

        bundle.putString(
            Constants.CallKeys.CHANNEL,
            channel
        )

        bundle.putBoolean(
            Constants.CallKeys.IS_VIDEO_CALL,
            isVideoCall
        )

        bundle.putInt(
            Constants.CallKeys.CONTACT_ID,
            contactId
        )

        service.putExtras(bundle)

        context.startService(service)
    }
}