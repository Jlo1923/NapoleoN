package com.naposystems.napoleonchat.di.module.general

import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.util.HttpAuthorizer
import dagger.Module
import dagger.Provides
import io.socket.client.IO
import io.socket.client.Socket
import javax.inject.Singleton

@Module
class PusherModule {

    @Provides
    @Singleton
    fun providePusher(sharedPreferencesManager: SharedPreferencesManager): Pusher {

        val pusherOptions = PusherOptions()
        pusherOptions.activityTimeout = 180000
        pusherOptions.maxReconnectGapInSeconds = 4
        pusherOptions.maxReconnectionAttempts = 3

        val authorizer = HttpAuthorizer(BuildConfig.SOCKET_BASE_URL)

        val mapAuth = HashMap<String, String>()

        mapAuth["X-API-Key"] =
            sharedPreferencesManager.getString(Constants.SharedPreferences.PREF_FIREBASE_ID, "")
        authorizer.setHeaders(mapAuth)

        pusherOptions.setCluster("us2")

        pusherOptions.authorizer = authorizer

        return Pusher(BuildConfig.PUSHER_KEY, pusherOptions)
    }

    @Provides
    @Singleton
    fun provideSocket(): Socket = IO.socket(BuildConfig.SOCKET_BASE_URL)

}