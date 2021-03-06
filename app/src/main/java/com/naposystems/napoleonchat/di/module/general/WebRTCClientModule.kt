package com.naposystems.napoleonchat.di.module.general

import com.naposystems.napoleonchat.webRTC.IContractWebRTCClient
import com.naposystems.napoleonchat.webRTC.WebRTCClient
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class WebRTCClientModule {

    @Binds
    @Singleton
    abstract fun bindWebRTCClient(webRTCClient: WebRTCClient): IContractWebRTCClient

}