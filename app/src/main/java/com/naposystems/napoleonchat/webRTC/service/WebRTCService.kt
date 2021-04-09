package com.naposystems.napoleonchat.webRTC.service

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.model.CallModel
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.notificationClient.HandlerNotification
import com.naposystems.napoleonchat.service.notificationClient.HandlerNotificationImp
import com.naposystems.napoleonchat.service.notificationClient.NotificationClient
import com.naposystems.napoleonchat.ui.conversationCall.ConversationCallActivity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.adapters.hasMicAndCameraPermission
import com.naposystems.napoleonchat.utils.handlerMediPlayer.HandlerMediaPlayerNotification
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject

class WebRTCService : Service() {

    companion object {
        const val ACTION_ANSWER_CALL = "ANSWER_CALL"
        const val ACTION_DENY_CALL = "DENY_CALL"
        const val ACTION_CALL_END = "CALL_END"
        const val ACTION_HANG_UP = "HANG_UP"
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

        var callModel = CallModel(
            channelName = "",
            contactId = 0,
            isVideoCall = false,
            typeCall = Constants.TypeCall.IS_OUTGOING_CALL,
            offer = ""
        )

        intent.extras?.let { bundle ->
            if (bundle.containsKey(Constants.CallKeys.CALL_MODEL)) {
                callModel =
                    bundle.getSerializable(Constants.CallKeys.CALL_MODEL) as CallModel
                Timber.d("LLAMADA OBTENIENDO EXTRAS: STARTWEBRTCSERVICE callModel $callModel")
            }
        }

        intent.action?.let { action ->

            handlerMediaPlayerNotification.stopRingtone()

            Timber.d("LLAMADA PASO: ACTION $action")

            when (action) {
                ACTION_ANSWER_CALL -> {
                    Timber.d("LLAMADA PASO: CONTESTANDO LLAMADA")
                    startConversationCallActivity(
                        action = ACTION_ANSWER_CALL,
                        callModel = callModel
                    )
                }
                ACTION_DENY_CALL -> {

                    Timber.d("LLAMADA PASO: RECHAZANDO LLAMADA")
                    repository.rejectCall(callModel)
                    if (NapoleonApplication.isShowingCallActivity)
                        RxBus.publish(RxEvent.HangupByNotification(callModel.channelName))
                    stopForeground(true)
                    stopSelf()
                }
                ACTION_CALL_END -> {
                    Timber.d("LLAMADA PASO: LLAMADA FINALIZADA")
                    stopForeground(true)
                    stopSelf()
                }
                ACTION_HANG_UP -> {
                    Timber.d("LLAMADA PASO: COLGANDO LLAMADA")
                    RxBus.publish(RxEvent.HangupByNotification(callModel.channelName))
                    stopForeground(true)
                    stopSelf()
                }
                else ->
                    Timber.e("Action no recognized")

            }
        } ?: run {

            Timber.d("LLAMADA PASO 2: RUN")

            showCallNotification(callModel)

            if (NapoleonApplication.isVisible && NapoleonApplication.isShowingCallActivity.not()) {

                Timber.d("LLAMADA PASO 3: MOSTRAR ACTIVITY CALL")

                startConversationCallActivity(callModel = callModel)
            }
        }
        return START_NOT_STICKY
    }

    private fun showCallNotification(callModel: CallModel) {

        Timber.d("LLAMADA PASO: MOSTRANDO NOTIFICACION $callModel")

        if (callModel.channelName != "" && callModel.contactId > 0 && this.hasMicAndCameraPermission()) {

            callModel.typeCall = if (callModel.offer != "") Constants.TypeCall.IS_INCOMING_CALL
            else Constants.TypeCall.IS_OUTGOING_CALL

            val notification = handlerNotification.createNotificationCallBuilder(callModel)

            startForeground(HandlerNotificationImp.NOTIFICATION_CALL_ACTIVE, notification)

        }
    }

    private fun startConversationCallActivity(action: String = "", callModel: CallModel) {

        Timber.d("LLAMADA PASO: INICIANDO CONVERSATION: ACTION: $action, CALLMODEL: $callModel")

        if (this.hasMicAndCameraPermission()) {

            val intent = Intent(this, ConversationCallActivity::class.java).apply {
                putExtras(Bundle().apply {
                    putSerializable(ConversationCallActivity.KEY_CALL_MODEL, callModel)
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
}