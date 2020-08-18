package com.naposystems.napoleonchat.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.naposystems.napoleonchat.utility.Constants.SharedPreferences.PREF_FIREBASE_ID
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.notificationUtils.NotificationUtils

class NapoleonFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(newToken: String) {
        val sharedPreferencesManager = SharedPreferencesManager(applicationContext)
        sharedPreferencesManager.putString(PREF_FIREBASE_ID, newToken)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val notificationUtils =
            NotificationUtils(
                applicationContext
            )
        notificationUtils.createInformativeNotification(
            applicationContext,
            remoteMessage.data,
            remoteMessage.notification
        )
    }
}