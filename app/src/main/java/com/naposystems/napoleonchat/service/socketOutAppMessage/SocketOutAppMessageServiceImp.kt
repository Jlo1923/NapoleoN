package com.naposystems.napoleonchat.service.socketOutAppMessage

import android.content.Context
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.syncManager.SyncManager
import com.naposystems.napoleonchat.source.remote.dto.validateMessageEvent.ValidateMessage
import com.naposystems.napoleonchat.source.remote.dto.validateMessageEvent.ValidateMessageEventDTO
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.pusher.client.Pusher
import com.pusher.client.channel.PrivateChannel
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.channel.PusherEvent
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import com.squareup.moshi.Moshi
import timber.log.Timber
import javax.inject.Inject

class SocketOutAppMessageServiceImp @Inject constructor(
    private val context: Context,
    private val pusher: Pusher,
    private val sharedPreferencesManager: SharedPreferencesManager,
    syncManager: SyncManager
) : SocketOutAppMessageService {

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    private val app: NapoleonApplication by lazy {
        context as NapoleonApplication
    }

    private var userId = syncManager.getUserId()

    private lateinit var globalChannel: PrivateChannel

    init {

        Timber.d("Pusher Paso OUT: //////////////////////////////////////")

    }

    //region Implementacion Interfaz

    override fun getStatusSocket(): ConnectionState {
        return pusher.connection.state
    }

    override fun getStatusGlobalChannel(): Boolean {
        return if (pusher.getPrivateChannel(Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName) != null)
            if (pusher.getPrivateChannel(Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName).isSubscribed)
                if (::globalChannel.isInitialized)
                    globalChannel.isSubscribed
                else
                    Constants.SocketChannelStatus.SOCKECT_CHANNEL_STATUS_NOT_CONNECTED.status
            else
                Constants.SocketChannelStatus.SOCKECT_CHANNEL_STATUS_NOT_CONNECTED.status
        else
            Constants.SocketChannelStatus.SOCKECT_CHANNEL_STATUS_NOT_CONNECTED.status
    }

    override fun connectSocket() {

        Timber.d("Pusher Paso OUT 1: *****************")

        Timber.d("Pusher Paso OUT 1: connectSocket: State:${pusher.connection.state}")

        Timber.d("Pusher Paso OUT 1: userId: $userId")

        if (userId != Constants.UserNotExist.USER_NO_EXIST.user) {

            if (pusher.connection.state == ConnectionState.DISCONNECTED ||
                pusher.connection.state == ConnectionState.DISCONNECTING
            ) {

                pusher.connect(object : ConnectionEventListener {

                    override fun onConnectionStateChange(change: ConnectionStateChange?) {

                        if (change?.currentState == ConnectionState.CONNECTED) {

                            Timber.d("Pusher Paso OUT 1.2: CONECTADO")

                            subscribeChannel()

                        }

                    }

                    override fun onError(message: String?, code: String?, e: java.lang.Exception?) {

                        Timber.d("Pusher Paso OUT 1.3: connectSocket: onError $message, code: $code")

                        pusher.connect()

                    }

                })
            } else if (pusher.connection.state == ConnectionState.CONNECTED && !app.isAppVisible()) {

                Timber.d("Pusher Paso OUT 1.4: Ya Conectado")

                subscribeChannel()
            }
        }
    }

    private fun subscribeChannel() {

        Timber.d("Pusher Paso OUT 2: subscribeChannels")

        try {
            sharedPreferencesManager.putString(
                Constants.SharedPreferences.PREF_SOCKET_ID,
                pusher.connection.socketId
            )

            Timber.d("Pusher Paso OUT 2.1: State:${pusher.connection.state}, SocketId:${pusher.connection.socketId}")

            unsubscribeChannel()

            subscribeToPrivateGlobalChannel()

        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun subscribeToPrivateGlobalChannel() {

        Timber.d("Pusher Paso OUT 3: subscribeToPrivateGlobalChannel")

        try {

            if (pusher.getPrivateChannel(Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName) == null) {

                Timber.d("Pusher Paso OUT 3.1: Dentro del if")

                globalChannel = pusher.subscribePrivate(
                    Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName,
                    object : PrivateChannelEventListener {
                        override fun onEvent(event: PusherEvent?) {
                            Timber.d("Pusher Paso OUT 3.2: subscribeToPrivateGlobalChannel: onEvent ${event?.data}")
                        }

                        override fun onAuthenticationFailure(
                            message: String?,
                            e: java.lang.Exception?
                        ) {
                            Timber.d("Pusher Paso OUT: subscribeToPrivateGlobalChannel: onAuthenticationFailure")
                        }

                        override fun onSubscriptionSucceeded(channelName: String?) {

                            Timber.d("Pusher Paso OUT 3.3: subscribeToPrivateGlobalChannel: onSubscriptionSucceeded:$channelName")

                            RxBus.publish(RxEvent.CreateNotification())

                        }
                    }
                )
            }

        } catch (e: Exception) {
            Timber.e("Pusher Paso OUT 3.4:  subscribeToPrivateGlobalChannel: Exception: $e")
        }
    }

    override fun emitClientConversation(messages: List<ValidateMessage>) {

        try {

            val validateMessage = ValidateMessageEventDTO(messages)

            val adapterValidate = moshi.adapter(ValidateMessageEventDTO::class.java)

            val jsonObject = adapterValidate.toJson(validateMessage)

            if (jsonObject.isNotEmpty())

                globalChannel.trigger(
                    Constants.SocketEmitTriggers.CLIENT_CONVERSATION.trigger,
                    jsonObject
                )

        } catch (e: Exception) {
            Timber.e(e)
        }

    }

    private fun unsubscribeChannel() {
        Timber.d("Pusher Paso OUT 2.2: Desubscribirse")
//        if (::globalChannel.isInitialized)
//            if (pusher.getPrivateChannel(Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName) != null)
//                if (pusher.getPrivateChannel(Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName).isSubscribed)
        pusher.unsubscribe(Constants.SocketChannelName.PRIVATE_GLOBAL_CHANNEL_NAME.channelName)

//        if (pusher.getPrivateChannel(Constants.SocketChannelName.PRIVATE_GENERAL_CHANNEL_NAME.channelName + userId) != null)
//            if (pusher.getPrivateChannel(Constants.SocketChannelName.PRIVATE_GENERAL_CHANNEL_NAME.channelName + userId).isSubscribed)
        pusher.unsubscribe(Constants.SocketChannelName.PRIVATE_GENERAL_CHANNEL_NAME.channelName + userId)

    }

    //endregion

}