package com.naposystems.pepito.webService

import com.naposystems.pepito.reactive.RxBus
import com.naposystems.pepito.reactive.RxEvent
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.webService.service.IContractSocketService
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class SocketService @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractSocketService {

    private val socket: Socket by lazy {
        IO.socket(Constants.NapoleonApi.SOCKET_BASE_URL)
    }

    init {
        try {
            socket.connect().on(Socket.EVENT_CONNECT) {

                sharedPreferencesManager.putString(
                    Constants.SharedPreferences.PREF_SOCKET_ID,
                    socket.id()
                )
                Timber.e("Conectó al socket ${socket.id()}")
            }
                .on(Socket.EVENT_CONNECT_ERROR) {
                    Timber.e("No conectó al socket $it")
                }

            listenNewMessageEvent()

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

    private fun listenNewMessageEvent() {
        socket.on("App\\Events\\NewMessageEvent") {
            Timber.d("NewMessageEvent")
            RxBus.publish(RxEvent.NewMessageReceivedEvent(it[0] as String, it[1] as JSONObject))
        }
    }
}