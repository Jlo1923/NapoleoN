package com.naposystems.napoleonchat.di.module.general

import android.content.Context
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.webService.socket.IContractSocketService
import com.naposystems.napoleonchat.webService.socket.SocketService
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.util.HttpAuthorizer
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import io.socket.client.IO
import io.socket.client.Socket
import javax.inject.Singleton

@Module
class SocketModule {

    @Provides
    @Singleton
    fun providePusher(sharedPreferencesManager: SharedPreferencesManager): Pusher {
        val pusherOptions = PusherOptions()

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


    @Provides
    @Singleton
    fun provideSocketClient(
        moshi: Moshi,
        context: Context,
        socket: Pusher,
        sharedPreferencesManager: SharedPreferencesManager,
        socketRepository: IContractSocketService.Repository
    ): IContractSocketService.SocketService {
        return SocketService(
            moshi,
            context,
            socket,
            sharedPreferencesManager,
            socketRepository
        )
    }

}