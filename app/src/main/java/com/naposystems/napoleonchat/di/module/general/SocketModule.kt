package com.naposystems.napoleonchat.di.module.general

import com.naposystems.napoleonchat.service.socketClient.SocketClient
import com.naposystems.napoleonchat.service.socketClient.SocketClientImp
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class SocketModule {

    @Binds
    @Singleton
    abstract fun bindSocketMessageService(socketMessageService: SocketClientImp): SocketClient

}