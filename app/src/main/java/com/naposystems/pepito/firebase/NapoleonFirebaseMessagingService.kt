package com.naposystems.pepito.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.naposystems.pepito.utility.Constants.SharedPreferences.PREF_FIREBASE_ID
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.utility.notificationUtils.NotificationUtils
import timber.log.Timber

class NapoleonFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(newToken: String) {
        Timber.d("Firebase token: $newToken")
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