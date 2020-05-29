package com.naposystems.pepito.service.webRTCCall

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import com.naposystems.pepito.repository.webRTCCallService.WebRTCCallServiceRepository
import com.naposystems.pepito.ui.conversationCall.ConversationCallActivity
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.adapters.hasMicAndCameraPermission
import com.naposystems.pepito.utility.notificationUtils.NotificationUtils
import dagger.android.support.DaggerApplication
import timber.log.Timber
import javax.inject.Inject

class WebRTCCallService : Service() {

    companion object {
        const val ACTION_ANSWER_CALL = "ANSWER_CALL"
        const val ACTION_DENY_CALL = "DENY_CALL"
        const val ACTION_CALL_CONNECTED = "CALL_CONNECTED"
        const val ACTION_CALL_END = "CALL_END"
    }

    @Inject
    lateinit var repository: WebRTCCallServiceRepository

    override fun onCreate() {
        super.onCreate()
        (applicationContext as DaggerApplication).androidInjector().inject(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(nullableIntent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand")
        nullableIntent?.let { intent ->
            intent.action?.let { action ->
                Timber.d("onStartCommand action: $action")
                when (action) {
                    ACTION_ANSWER_CALL -> {
                        intent.extras?.let { bundle ->
                            startConversationCallActivity(
                                bundle,
                                ACTION_ANSWER_CALL
                            )
                        }
                    }
                    ACTION_DENY_CALL -> {
                        intent.extras?.let { bundle ->
                            var channel = ""
                            var contactId = 0

                            if (bundle.containsKey(Constants.CallKeys.CHANNEL)) {
                                channel = bundle.getString(Constants.CallKeys.CHANNEL) ?: ""
                            }

                            if (bundle.containsKey(Constants.CallKeys.CONTACT_ID)) {
                                contactId = bundle.getInt(Constants.CallKeys.CONTACT_ID, 0)
                            }

                            repository.rejectCall(contactId, channel)
                            stopForeground(true)
                            stopSelf()
                        }
                    }
                    ACTION_CALL_CONNECTED, ACTION_CALL_END -> {
                        stopForeground(true)
                        stopSelf()
                    }
                    else -> {
                        //Intentionally empty
                    }
                }
            } ?: run {
                intent.extras?.let { bundle ->
                    Timber.d("onStartCommand bundle")
                    if (Build.VERSION.SDK_INT >= 29) {
                        getExtrasAndShowCallNotif(bundle)
                    } else {
                        startConversationCallActivity(bundle)
                    }
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun getExtrasAndShowCallNotif(
        bundle: Bundle
    ) {
        var channel = ""
        var contactId = 0
        var isVideoCall = false

        if (bundle.containsKey(Constants.CallKeys.CHANNEL)) {
            channel = bundle.getString(Constants.CallKeys.CHANNEL) ?: ""
        }

        if (bundle.containsKey(Constants.CallKeys.CONTACT_ID)) {
            contactId = bundle.getInt(Constants.CallKeys.CONTACT_ID, 0)
        }

        if (bundle.containsKey(Constants.CallKeys.IS_VIDEO_CALL)) {
            isVideoCall = bundle.getBoolean(Constants.CallKeys.IS_VIDEO_CALL, false)
        }

        if (channel.isNotEmpty() && contactId > 0 && this.hasMicAndCameraPermission()) {
            val notificationUtils =
                NotificationUtils(
                    applicationContext
                )
            val notification = notificationUtils.createCallNotification(
                channel,
                contactId,
                isVideoCall,
                applicationContext
            )

            val notificationId = notificationUtils.getNotificationId(
                applicationContext,
                Constants.NotificationType.INCOMING_CALL.type
            )
            Timber.d("notificationId: $notificationId")

            startForeground(
                notificationUtils.getNotificationId(
                    applicationContext,
                    Constants.NotificationType.INCOMING_CALL.type
                ), notification
            )
        }
    }

    private fun startConversationCallActivity(bundle: Bundle, action: String = "") {
        if (this.hasMicAndCameraPermission()) {
            var channel = ""
            var contactId = 0
            var isVideoCall = false

            if (bundle.containsKey(Constants.CallKeys.CHANNEL)) {
                channel = bundle.getString(Constants.CallKeys.CHANNEL) ?: ""
            }

            if (bundle.containsKey(Constants.CallKeys.CONTACT_ID)) {
                contactId = bundle.getInt(Constants.CallKeys.CONTACT_ID, 0)
            }

            if (bundle.containsKey(Constants.CallKeys.IS_VIDEO_CALL)) {
                isVideoCall = bundle.getBoolean(Constants.CallKeys.IS_VIDEO_CALL, false)
            }

            if (channel.isNotEmpty() && contactId > 0) {

                val newIntent = Intent(this, ConversationCallActivity::class.java).apply {
                    putExtras(Bundle().apply {
                        putInt(ConversationCallActivity.CONTACT_ID, contactId)
                        putString(ConversationCallActivity.CHANNEL, channel)
                        putBoolean(ConversationCallActivity.IS_VIDEO_CALL, isVideoCall)
                        putBoolean(ConversationCallActivity.IS_INCOMING_CALL, true)
                        putBoolean(ConversationCallActivity.IS_FROM_CLOSED_APP, true)
                    })
                }

                if (action.isNotEmpty()) {
                    newIntent.action = action
                }

                newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                startActivity(newIntent)
                stopForeground(true)

            }
        }
    }
}