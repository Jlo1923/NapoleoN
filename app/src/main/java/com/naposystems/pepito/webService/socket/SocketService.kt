package com.naposystems.pepito.webService.socket

import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class SocketService @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val repository: IContractSocketService.Repository
) : IContractSocketService.SocketService {

    private val socket: Socket by lazy {
        IO.socket(Constants.NapoleonApi.SOCKET_BASE_URL)
    }

    init {
        try {
            connectToSocket()

            listenOnDisconnect()

            listenNewMessageEvent()

            listenNotifyMessagesReceived()

            listenMessagesRead()

            listenMessagesDestroy()

        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun subscribe(jsonObject: JSONObject) {
        socket.emit("subscribe", jsonObject)
        Timber.d("Subscribe to $jsonObject")
    }

    override fun unSubscribe(jsonObject: JSONObject, channelName: String) {
        socket.off(channelName)
        socket.emit("unsubscribe", jsonObject)
        Timber.d("unsubscribe to channel: $channelName")
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

        }.on(Socket.EVENT_CONNECT_ERROR) {
            Timber.e("No conectó al socket $it")
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
}