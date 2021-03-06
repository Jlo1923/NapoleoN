package com.naposystems.napoleonchat.di.module.general

import com.naposystems.napoleonchat.webService.socket.IContractSocketService
import com.naposystems.napoleonchat.webService.socket.SocketService
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class SocketModule {

    @Binds
    @Singleton
    abstract fun bindSocketClient(socketService: SocketService): IContractSocketService.SocketService

}