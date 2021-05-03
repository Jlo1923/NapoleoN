package com.naposystems.napoleonchat.service.notificationClient

import com.google.firebase.messaging.RemoteMessage
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.utility.Constants
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

            Constants.NotificationType.VERIFICATION_CODE.type,
            Constants.NotificationType.SUBSCRIPTION.type -> {
                handlerNotification.showNotification(
                    dataFromNotification,
                    notification
                )
            }

            Constants.NotificationType.ENCRYPTED_MESSAGE.type -> {
                if (NapoleonApplication.isVisible.not())
                    handlerNotificationMessage.handlerMessage(dataFromNotification, notification)
            }

            Constants.NotificationType.NEW_FRIENDSHIP_REQUEST.type -> {
                RxBus.publish(RxEvent.NewFriendshipRequest())
            }

            Constants.NotificationType.FRIEND_REQUEST_ACCEPTED.type -> {
                RxBus.publish(RxEvent.FriendshipRequestAccepted())
            }

            Constants.NotificationType.ACCOUNT_ATTACK.type -> {

                sharedPreferencesManager.putInt(
                    Constants.SharedPreferences.PREF_EXISTING_ATTACK,
                    Constants.ExistingAttack.EXISTING.type
                )

                sharedPreferencesManager.putString(
                    Constants.SharedPreferences.PREF_ATTACKER_ID,
                    dataFromNotification.getValue(Constants.NotificationKeys.ATTACKER_ID).toString()
                )
                handlerNotification.showNotification(
                    dataFromNotification,
                    notification
                )

                RxBus.publish(RxEvent.AccountAttack())
            }

            Constants.NotificationType.INCOMING_CALL.type -> {

                handlerNotificationCall.handlerCall(dataFromNotification)

            }
        }
    }
}
