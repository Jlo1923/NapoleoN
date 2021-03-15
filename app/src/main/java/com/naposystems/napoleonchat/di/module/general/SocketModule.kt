package com.naposystems.napoleonchat.di.module.general

import com.naposystems.napoleonchat.service.socketInAppMessage.SocketMessageService
import com.naposystems.napoleonchat.service.socketInAppMessage.SocketMessageServiceImp
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class SocketModule {

    @Binds
    @Singleton
    abstract fun bindSocketMessageService(socketInAppMessageService: SocketMessageServiceImp): SocketMessageService

}