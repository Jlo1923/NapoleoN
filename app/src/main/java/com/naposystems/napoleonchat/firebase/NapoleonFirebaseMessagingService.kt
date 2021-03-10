package com.naposystems.napoleonchat.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.naposystems.napoleonchat.service.notification.NotificationService
import com.naposystems.napoleonchat.utility.Constants.SharedPreferences.PREF_FIREBASE_ID
import com.naposystems.napoleonchat.utility.SharedPreferencesManager

class NapoleonFirebaseMessagingService :
    FirebaseMessagingService() {

    override fun onNewToken(newToken: String) {
        val sharedPreferencesManager = SharedPreferencesManager(applicationContext)
        sharedPreferencesManager.putString(PREF_FIREBASE_ID, newToken)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        val notificationService = NotificationService(applicationContext, null)

        notificationService.createInformativeNotification(
            applicationContext,
            remoteMessage.data,
            remoteMessage.notification
        )
    }
}