package com.naposystems.pepito.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.naposystems.pepito.utility.Constants.SharedPreferences.PREF_FIREBASE_ID
import com.naposystems.pepito.utility.NotificationUtils
import com.naposystems.pepito.utility.SharedPreferencesManager
import timber.log.Timber
import javax.inject.Inject

class NapoleonFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(newToken: String) {
        Timber.d("Firebase token: $newToken")
        val sharedPreferencesManager = SharedPreferencesManager(applicationContext)
        sharedPreferencesManager.putString(PREF_FIREBASE_ID, newToken)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        NotificationUtils.createInformativeNotification(this, remoteMessage.data)
    }
}