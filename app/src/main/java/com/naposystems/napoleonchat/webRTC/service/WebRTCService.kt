package com.naposystems.napoleonchat.webRTC.service

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.notificationMessage.NotificationMessagesService
import com.naposystems.napoleonchat.service.notificationMessage.NotificationMessagesServiceImp
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
    lateinit var notificationMessagesService: NotificationMessagesService

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

        var channelName = ""
        var contactId = 0
        var isVideoCall = false
        var isIncomingCall = false
        var offer = ""

        intent.extras?.let { bundle ->
            if (bundle.containsKey(Constants.CallKeys.CHANNEL_NAME))
                channelName = bundle.getString(Constants.CallKeys.CHANNEL_NAME) ?: ""


            if (bundle.containsKey(Constants.CallKeys.CONTACT_ID))
                contactId = bundle.getInt(Constants.CallKeys.CONTACT_ID, 0)


            if (bundle.containsKey(Constants.CallKeys.IS_VIDEO_CALL))
                isVideoCall = bundle.getBoolean(Constants.CallKeys.IS_VIDEO_CALL, false)


            if (bundle.containsKey(Constants.CallKeys.IS_INCOMING_CALL))
                isIncomingCall = bundle.getBoolean(Constants.CallKeys.IS_INCOMING_CALL, false)


            if (bundle.containsKey(Constants.CallKeys.OFFER))
                offer = bundle.getString(Constants.CallKeys.OFFER, "")

        }

        intent.action?.let { action ->
            Timber.d("onStartCommand action: $action")
            notificationMessagesService.stopMediaPlayer()
            when (action) {
                ACTION_ANSWER_CALL -> {
                    startConversationCallActivity(
                        channel = channelName,
                        contactId = contactId,
                        isVideoCall = isVideoCall,
                        offer = offer,
                        action = ACTION_ANSWER_CALL
                    )
                }
                ACTION_DENY_CALL -> {
                    repository.rejectCall(contactId, channelName)
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
                    RxBus.publish(RxEvent.HangupByNotification(channelName))
                }
                else -> {
                }
            }
        } ?: run {
            if (isIncomingCall) {
                showIncomingCallNotification(channelName, contactId, isVideoCall, offer)
                if (!napoleonApplication.visible) {
                    startConversationCallActivity(channelName, contactId, isVideoCall, offer)
                }
            } else {
                showOutgoingCallNotification(channelName, contactId, isVideoCall)
            }
        }
        return START_NOT_STICKY
    }

    private fun showIncomingCallNotification(
        channel: String,
        contactId: Int,
        isVideoCall: Boolean,
        offer: String
    ) {
        if (channel.isNotEmpty() && contactId > 0 && this.hasMicAndCameraPermission() && offer.isNotEmpty()) {
            val notification = notificationMessagesService.createNotificationCallBuilder(
                channel,
                contactId,
                isVideoCall,
                Constants.TypeCall.IS_INCOMING_CALL.type,
                offer
            )
            startForeground(NotificationMessagesServiceImp.NOTIFICATION_RINGING, notification)
        }
    }

    private fun showOutgoingCallNotification(
        channelName: String,
        contactId: Int,
        isVideoCall: Boolean
    ) {
        if (channelName.isNotEmpty() && contactId > 0 && this.hasMicAndCameraPermission()) {
            val notification = notificationMessagesService.createNotificationCallBuilder(
                channelName,
                contactId,
                isVideoCall,
                Constants.TypeCall.IS_OUTGOING_CALL.type,
            )
            startForeground(NotificationMessagesServiceImp.NOTIFICATION_RINGING, notification)
        }
    }

    private fun startConversationCallActivity(
        channel: String,
        contactId: Int,
        isVideoCall: Boolean,
        offer: String,
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
                        putInt(
                            ConversationCallActivity.TYPE_CALL,
                            Constants.TypeCall.IS_INCOMING_CALL.type
                        )
                        putString(ConversationCallActivity.OFFER, offer)
                        putBoolean(ConversationCallActivity.IS_FROM_CLOSED_APP, true)
                        putBoolean(
                            ConversationCallActivity.ANSWER_CALL,
                            action == ACTION_ANSWER_CALL
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