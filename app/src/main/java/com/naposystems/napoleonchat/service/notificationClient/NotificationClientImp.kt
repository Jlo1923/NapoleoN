package com.naposystems.napoleonchat.service.notificationClient

import com.google.firebase.messaging.RemoteMessage
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Constants.ExistingAttack.EXISTING
import com.naposystems.napoleonchat.utility.Constants.NotificationKeys.ATTACKER_ID
import com.naposystems.napoleonchat.utility.Constants.NotificationType.*
import com.naposystems.napoleonchat.utility.Constants.SharedPreferences.PREF_ATTACKER_ID
import com.naposystems.napoleonchat.utility.Constants.SharedPreferences.PREF_EXISTING_ATTACK
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utils.handlerNotificationChannel.HandlerNotificationChannel
import javax.inject.Inject

class NotificationClientImp
@Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager,
    handlerNotificationChannel: HandlerNotificationChannel,
    private val handlerNotification: HandlerNotification,
    private val handlerNotificationMessage: HandlerNotificationMessage,
    private val handlerNotificationCall: HandlerNotificationCall
) : NotificationClient {

    init {
        handlerNotificationChannel.initializeChannels()
    }

    override fun createNotification(
        dataFromNotification: Map<String, String>,
        notification: RemoteMessage.Notification?
    ) {
        when (dataFromNotification.getValue(Constants.NotificationKeys.TYPE_NOTIFICATION).toInt()) {
            VERIFICATION_CODE.type, SUBSCRIPTION.type -> handlerNotification.showNotification(
                dataFromNotification,
                notification
            )
            ENCRYPTED_MESSAGE.type -> if (NapoleonApplication.isVisible.not()) {
                handlerNotificationMessage.handlerMessage(dataFromNotification, notification)
            }
            NEW_FRIENDSHIP_REQUEST.type -> RxBus.publish(RxEvent.NewFriendshipRequest())
            FRIEND_REQUEST_ACCEPTED.type -> RxBus.publish(RxEvent.FriendshipRequestAccepted())
            ACCOUNT_ATTACK.type -> handleAccountAttackCase(dataFromNotification, notification)
            INCOMING_CALL.type -> handlerNotificationCall.handlerCall(dataFromNotification)
        }
    }

    private fun handleAccountAttackCase(
        dataFromNotification: Map<String, String>,
        notification: RemoteMessage.Notification?
    ) {
        sharedPreferencesManager.apply {
            putInt(PREF_EXISTING_ATTACK, EXISTING.type)
            val prefAttackerId = dataFromNotification.getValue(ATTACKER_ID).toString()
            putString(PREF_ATTACKER_ID, prefAttackerId)
        }
        handlerNotification.showNotification(dataFromNotification, notification)
        RxBus.publish(RxEvent.AccountAttack())
    }
}
