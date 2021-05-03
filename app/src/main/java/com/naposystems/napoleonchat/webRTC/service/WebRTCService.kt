package com.naposystems.napoleonchat.webRTC.service

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.notificationClient.HandlerNotification
import com.naposystems.napoleonchat.service.notificationClient.HandlerNotificationImp
import com.naposystems.napoleonchat.service.notificationClient.NotificationClient
import com.naposystems.napoleonchat.ui.conversationCall.ConversationCallActivity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.TypeEndCallEnum
import com.naposystems.napoleonchat.utility.adapters.hasMicAndCameraPermission
import com.naposystems.napoleonchat.utility.isNoCall
import com.naposystems.napoleonchat.utils.handlerMediPlayer.HandlerMediaPlayerNotification
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject

class WebRTCService : Service() {

    companion object {
        const val ACTION_ANSWER_CALL = "ANSWER_CALL"
        const val ACTION_DENY_CALL = "DENY_CALL"
        const val ACTION_CALL_END = "CALL_END"
        const val ACTION_FAILED_CALL_END = "FAILED_CALL_END"
        const val ACTION_HANG_UP = "HANG_UP"
        const val ACTION_OPEN_CALL = "OPEN_CALL"
        const val ACTION_HIDE_NOTIFICATION = "HIDE_NOTIFICATION"
    }

    @Inject
    lateinit var notificationClient: NotificationClient

    @Inject
    lateinit var handlerNotification: HandlerNotification

    @Inject
    lateinit var handlerMediaPlayerNotification: HandlerMediaPlayerNotification

    @Inject
    lateinit var repository: WebRTCServiceRepositoryImp

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        Timber.d("LLAMADA PASO 1: INICIANDO SERVICIO")

//        var callModel = CallModel()

//        intent.extras?.let { bundle ->
//            if (bundle.containsKey(Constants.CallKeys.CALL_MODEL)) {
//                callModel =
//                    bundle.getSerializable(Constants.CallKeys.CALL_MODEL) as CallModel
//                Timber.d("LLAMADA OBTENIENDO EXTRAS: STARTWEBRTCSERVICE")
//            }
//        }

        intent.action?.let { action ->

            handlerMediaPlayerNotification.stopRingtone()

            Timber.d("LLAMADA PASO: ACTION $action")

            when (action) {
                ACTION_ANSWER_CALL -> {
                    Timber.d("LLAMADA PASO: CONTESTANDO LLAMADA")
                    startConversationCallActivity(
                        action = ACTION_ANSWER_CALL
                    )
                }
                ACTION_DENY_CALL -> {

                    Timber.d("LLAMADA PASO: RECHAZANDO LLAMADA")

                    if (NapoleonApplication.isShowingCallActivity) {
                        RxBus.publish(RxEvent.HangupByNotification())
                    } else {
                        repository.disposeCall(TypeEndCallEnum.TYPE_REJECT)
                    }

                    hideNotification()
                }

                ACTION_CALL_END -> {
                    Timber.d("LLAMADA PASO: LLAMADA FINALIZADA")

                    if (NapoleonApplication.isShowingCallActivity) {
                        RxBus.publish(RxEvent.HangupByNotification())
                    } else {
                        repository.disposeCall(TypeEndCallEnum.TYPE_CANCEL)
                    }

                    hideNotification()

                }

                ACTION_FAILED_CALL_END -> {
                    hideNotification()
                }

                ACTION_HANG_UP -> {

                    Timber.d("LLAMADA PASO: COLGANDO LLAMADA")

                    if (NapoleonApplication.isShowingCallActivity) {
                        RxBus.publish(RxEvent.HangupByNotification())
                    } else {
                        repository.disposeCall()
                    }

                    hideNotification()
                }

                ACTION_OPEN_CALL -> {
                    Timber.d("LLAMADA PASO: ABRIENDO LLAMADA EN SERVICIO")
                    startConversationCallActivity()
                }

                ACTION_HIDE_NOTIFICATION -> {
                    hideNotification()
                }

                else ->
                    Timber.e("Action no recognized")

            }
        } ?: run {

            Timber.d("LLAMADA PASO 2: RUN")

            showCallNotification()

            if (NapoleonApplication.isVisible &&
                NapoleonApplication.isShowingCallActivity.not() &&
                NapoleonApplication.statusCall.isNoCall()
            ) {

                Timber.d("LLAMADA PASO 3: MOSTRAR ACTIVITY CALL")

                startConversationCallActivity()
            }
        }
        return START_NOT_STICKY
    }

    private fun showCallNotification() {

        Timber.d("LLAMADA PASO: MOSTRANDO NOTIFICACION")

        NapoleonApplication.callInfoModel?.let { callModel ->

            if (callModel.channelName != "" && callModel.contactId > 0 && this.hasMicAndCameraPermission()) {

                callModel.typeCall = if (callModel.offer != "")
                    Constants.TypeCall.IS_INCOMING_CALL
                else
                    Constants.TypeCall.IS_OUTGOING_CALL

                val notification = handlerNotification.createNotificationCallBuilder()

                startForeground(HandlerNotificationImp.NOTIFICATION_CALL_ACTIVE, notification)
            }
        }

    }

    private fun startConversationCallActivity(action: String = "") {

        Timber.d("LLAMADA PASO: INICIANDO CONVERSATION: ACTION: $action")

        if (this.hasMicAndCameraPermission()) {

            val intent = Intent(this, ConversationCallActivity::class.java).apply {
                putExtras(Bundle().apply {
                    putBoolean(
                        ConversationCallActivity.ACTION_ANSWER_CALL,
                        action == ACTION_ANSWER_CALL
                    )
                })
            }

            if (NapoleonApplication.isVisible && action.isNotEmpty())
                intent.action = action

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            startActivity(intent)

        }

    }

    private fun hideNotification() {
        stopForeground(true)
        stopSelf()
    }

}