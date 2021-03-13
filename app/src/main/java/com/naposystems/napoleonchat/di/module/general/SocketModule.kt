package com.naposystems.napoleonchat.di.module.general

import com.naposystems.napoleonchat.service.socketOutAppMessage.SocketOutAppMessageService
import com.naposystems.napoleonchat.service.socketOutAppMessage.SocketOutAppMessageServiceImp
import com.naposystems.napoleonchat.service.socketInAppMessage.SocketInAppMessageService
import com.naposystems.napoleonchat.service.socketInAppMessage.SocketInAppMessageServiceImp
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class SocketModule {

    @Binds
    @Singleton
    abstract fun bindSocketService(socketServiceImp: SocketInAppMessageServiceImp): SocketInAppMessageService

    @Binds
    @Singleton
    abstract fun bindSocketNotificationService(socketNotificationServiceImp: SocketOutAppMessageServiceImp): SocketOutAppMessageService

}