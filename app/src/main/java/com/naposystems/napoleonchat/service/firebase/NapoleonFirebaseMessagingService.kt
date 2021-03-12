package com.naposystems.napoleonchat.service.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.naposystems.napoleonchat.service.notification.NotificationService
import com.naposystems.napoleonchat.utility.Constants.SharedPreferences.PREF_FIREBASE_ID
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import dagger.android.AndroidInjection
import javax.inject.Inject

class NapoleonFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationService: NotificationService

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onNewToken(newToken: String) {
        sharedPreferencesManager.putString(PREF_FIREBASE_ID, newToken)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        notificationService.createInformativeNotification(
            remoteMessage.data,
            remoteMessage.notification
        )
    }

}