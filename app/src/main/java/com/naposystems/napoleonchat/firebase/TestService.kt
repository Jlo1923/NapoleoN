package com.naposystems.napoleonchat.firebase

import android.app.Service
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.os.IBinder
import com.naposystems.napoleonchat.ui.conversationCall.ConversationCallActivity
import com.naposystems.napoleonchat.utility.Constants
import timber.log.Timber

class TestService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(nullableIntent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand")
        nullableIntent?.let { intent ->

            var channel = ""
            var contactId = 0
            var isVideoCall = false

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

                    newIntent.flags = FLAG_ACTIVITY_NEW_TASK

                    startActivity(newIntent)

                }
            }
        }

        return START_NOT_STICKY
    }
}
