package com.naposystems.napoleonchat.di.module.general

import com.naposystems.napoleonchat.service.notification.NotificationService
import com.naposystems.napoleonchat.service.notification.SocketNotificationService
import com.naposystems.napoleonchat.service.notification.SocketNotificationServiceImp
import com.naposystems.napoleonchat.service.socket.SocketService
import com.naposystems.napoleonchat.service.socket.SocketServiceImp
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class SocketModule {

    @Binds
    @Singleton
    abstract fun bindSocketService(socketServiceImp: SocketServiceImp): SocketService
    @Binds
    @Singleton
    abstract fun bindSocketNotificationService(socketNotificationServiceImp: SocketNotificationServiceImp): SocketNotificationService

}