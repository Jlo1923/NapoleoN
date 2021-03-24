package com.naposystems.napoleonchat.di.module.general

import com.naposystems.napoleonchat.service.socketMessage.SocketMessageService
import com.naposystems.napoleonchat.service.socketMessage.SocketMessageServiceImp
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class SocketModule {

    @Binds
    @Singleton
    abstract fun bindSocketMessageService(socketMessageService: SocketMessageServiceImp): SocketMessageService

}