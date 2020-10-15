package com.naposystems.napoleonchat.service.webRTCCall

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.repository.webRTCCallService.WebRTCCallServiceRepository
import com.naposystems.napoleonchat.ui.conversationCall.ConversationCallActivity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.adapters.hasMicAndCameraPermission
import com.naposystems.napoleonchat.utility.notificationUtils.NotificationUtils
import dagger.android.support.DaggerApplication
import timber.log.Timber
import javax.inject.Inject


class WebRTCCallService : Service() {

    companion object {
        const val ACTION_ANSWER_CALL = "ANSWER_CALL"
        const val ACTION_DENY_CALL = "DENY_CALL"
        const val ACTION_CALL_END = "CALL_END"
        const val ACTION_HANG_UP = "HANG_UP"
    }

    @Inject
    lateinit var repository: WebRTCCallServiceRepository

    private lateinit var napoleonApplication: NapoleonApplication

    val notificationUtils by lazy {
        NotificationUtils(
            applicationContext
        )
    }

    private val notificationId = NotificationUtils.NOTIFICATION_RINGING

    override fun onCreate() {
        super.onCreate()
        (applicationContext as DaggerApplication).androidInjector().inject(this)
        this.napoleonApplication = applicationContext as NapoleonApplication
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand")

        var channel = ""
        var contactId = 0
        var isVideoCall = false
        var isIncomingCall = false

        intent.extras?.let { bundle ->
            if (bundle.containsKey(Constants.CallKeys.CHANNEL)) {
                channel = bundle.getString(Constants.CallKeys.CHANNEL) ?: ""
            }

            if (bundle.containsKey(Constants.CallKeys.CONTACT_ID)) {
                contactId = bundle.getInt(Constants.CallKeys.CONTACT_ID, 0)
            }

            if (bundle.containsKey(Constants.CallKeys.IS_VIDEO_CALL)) {
                isVideoCall = bundle.getBoolean(Constants.CallKeys.IS_VIDEO_CALL, false)
            }

            if (bundle.containsKey(Constants.CallKeys.IS_INCOMING_CALL)) {
                isIncomingCall = bundle.getBoolean(Constants.CallKeys.IS_INCOMING_CALL, false)
            }
        }
        intent.action?.let { action ->
            Timber.d("onStartCommand action: $action")
            when (action) {
                ACTION_ANSWER_CALL -> {
                    startConversationCallActivity(
                        channel, contactId, isVideoCall,
                        ACTION_ANSWER_CALL
                    )
                }
                ACTION_DENY_CALL -> {
                    RxBus.publish(RxEvent.RejectCallByNotification(channel))
                    repository.rejectCall(contactId, channel)
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
                    RxBus.publish(RxEvent.HangupByNotification(channel))
                }
                else -> {
                }
            }
        } ?: run {
            Timber.d("onStartCommand bundle: $isIncomingCall")
            println("onStartCommand bundle: $isIncomingCall, $channel")
            if (isIncomingCall) {
                showIncomingCallNotification(channel, contactId, isVideoCall)
                if (!napoleonApplication.isAppVisible()) {
                    startConversationCallActivity(channel, contactId, isVideoCall)
                }
            } else {
                showCallingNotification(channel, contactId, isVideoCall)
            }
        }
        return START_NOT_STICKY
    }

    private fun showCallingNotification(channel: String, contactId: Int, isVideoCall: Boolean) {
        if (channel.isNotEmpty() && contactId > 0 && this.hasMicAndCameraPermission()) {

            val notification = notificationUtils.createCallingNotification(
                channel,
                contactId,
                isVideoCall,
                applicationContext
            )

            Timber.d("notificationId: $notificationId")

            startForeground(notificationId, notification)
        }
    }

    private fun showIncomingCallNotification(
        channel: String,
        contactId: Int,
        isVideoCall: Boolean
    ) {
        if (channel.isNotEmpty() && contactId > 0 && this.hasMicAndCameraPermission()) {

            val notification = notificationUtils.createCallNotification(
                channel,
                contactId,
                isVideoCall,
                applicationContext
            )

            Timber.d("notificationId: $notificationId")

            startForeground(notificationId, notification)
        }
    }

    private fun startConversationCallActivity(
        channel: String,
        contactId: Int,
        isVideoCall: Boolean,
        action: String = ""
    ) {
        if (this.hasMicAndCameraPermission()) {
            if (channel.isNotEmpty() && contactId > 0) {

                Timber.d("startCallActivity WebRTCCallService")
                val newIntent = Intent(this, ConversationCallActivity::class.java).apply {
                    putExtras(Bundle().apply {
                        putInt(ConversationCallActivity.CONTACT_ID, contactId)
                        putString(ConversationCallActivity.CHANNEL, channel)
                        putBoolean(ConversationCallActivity.IS_VIDEO_CALL, isVideoCall)
                        putBoolean(ConversationCallActivity.IS_INCOMING_CALL, true)
                        putBoolean(ConversationCallActivity.IS_FROM_CLOSED_APP, true)
                        putBoolean(
                            ConversationCallActivity.ANSWER_CALL,
                            action == ACTION_ANSWER_CALL
                        )
                    })
                }

                if (applicationContext is NapoleonApplication) {
                    val app = applicationContext as NapoleonApplication
                    if (app.isAppVisible() && action.isNotEmpty()) {
                        newIntent.action = action
                    }
                }
                /*if (action.isNotEmpty()) {
                    newIntent.action = action
                }*/

                newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                startActivity(newIntent)
                //stopForeground(true)
            }
        }
    }
}