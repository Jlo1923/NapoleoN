package com.naposystems.napoleonchat.service.notificationClient

import android.content.Context
import com.naposystems.napoleonchat.model.toCallModel
import com.naposystems.napoleonchat.service.socketClient.SocketClient
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class HandlerNotificationCallImp
@Inject constructor(
    private val context: Context,
    private val socketClient: SocketClient,
) : HandlerNotificationCall {

    var channel = ""
    var contactId = 0
    var isVideoCall = false
    var offer = ""

    private val disposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    override fun handlerCall(
        dataFromNotification: Map<String, String>
    ) {
        Timber.d("INCOMING OUTAPP PASO 2: $dataFromNotification")

        socketClient.connectSocket(true, dataFromNotification.toCallModel())

    }

}
