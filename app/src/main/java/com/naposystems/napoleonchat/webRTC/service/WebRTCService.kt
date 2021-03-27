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
    lateinit var napoleonApplication: NapoleonApplication

    @Inject
    lateinit var notificationClient: NotificationClient

    @Inject
    lateinit var handlerNotification: HandlerNotification

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

        var callModel = CallModel(
            channelName = "",
            contactId = 0,
            isVideoCall = false,
            typeCall = Constants.TypeCall.IS_OUTGOING_CALL,
            offer = ""
        )

        intent.extras?.let { bundle ->
            if (bundle.containsKey(Constants.CallKeys.CALL_MODEL))
                callModel = bundle.getSerializable(Constants.CallKeys.CALL_MODEL) as CallModel
        }

        intent.action?.let { action ->
            Timber.d("onStartCommand action: $action")
            handlerNotification.stopMediaPlayer()
            when (action) {
                ACTION_ANSWER_CALL -> {
                    startConversationCallActivity(
                        action = ACTION_ANSWER_CALL,
                        callModel = callModel
                    )
                }
                ACTION_DENY_CALL -> {
                    repository.rejectCall(callModel)
                    stopForeground(true)
                    stopSelf()
                }
                ACTION_CALL_END -> {
                    stopForeground(true)
                    stopSelf()
                }
                ACTION_HANG_UP -> {
                    stopForeground(true)
                    stopSelf()
                    RxBus.publish(RxEvent.HangupByNotification(callModel.channelName))
                }
                else -> {
                }
            }
        } ?: run {
            //TODO: Revisar aqui cuando llega la notificacion tambien muestra la pantalla  de llamada
            if (callModel.typeCall == Constants.TypeCall.IS_INCOMING_CALL) {
                showCallNotification(callModel)
                if (!napoleonApplication.visible) {
                    startConversationCallActivity(callModel = callModel)
                }
            } else {
                showCallNotification(callModel)
            }
        }
        return START_NOT_STICKY
    }

    private fun showCallNotification(
        callModel: CallModel
    ) {
        if (callModel.channelName != "" && callModel.contactId > 0 && this.hasMicAndCameraPermission()) {

            callModel.typeCall = if (callModel.offer != "") Constants.TypeCall.IS_INCOMING_CALL
            else Constants.TypeCall.IS_OUTGOING_CALL

            val notification = handlerNotification.createNotificationCallBuilder(
                callModel
            )
            startForeground(HandlerNotificationImp.NOTIFICATION_RINGING, notification)
        }
    }

    private fun startConversationCallActivity(
        action: String = "",
        callModel: CallModel
    ) {
        if (this.hasMicAndCameraPermission()) {
            if (callModel.channelName != "" && callModel.contactId > 0) {

                Timber.d("startCallActivity WebRTCCallService")
                val newIntent = Intent(this, ConversationCallActivity::class.java).apply {
                    putExtras(Bundle().apply {
                        callModel.typeCall = Constants.TypeCall.IS_INCOMING_CALL
                        putSerializable(ConversationCallActivity.CALL_MODEL, callModel)
                        putBoolean(ConversationCallActivity.IS_FROM_CLOSED_APP, true)
                        putBoolean(
                            ConversationCallActivity.ANSWER_CALL, action == ACTION_ANSWER_CALL
                        )
                    })
                }

                if (napoleonApplication.visible && action.isNotEmpty())
                    newIntent.action = action

                newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                startActivity(newIntent)
            }
        }
    }
}