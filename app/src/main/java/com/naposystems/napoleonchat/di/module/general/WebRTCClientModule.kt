package com.naposystems.napoleonchat.di.module.general

import com.naposystems.napoleonchat.webRTC.client.WebRTCClient
import com.naposystems.napoleonchat.webRTC.client.WebRTCClientImp
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class WebRTCClientModule {

    @Binds
    @Singleton
    abstract fun bindWebRTCClient(webRTCClient: WebRTCClientImp): WebRTCClient

}