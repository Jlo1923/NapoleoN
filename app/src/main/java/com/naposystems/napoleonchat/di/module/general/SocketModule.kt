package com.naposystems.napoleonchat.di.module.general

import com.naposystems.napoleonchat.service.notification.SocketNotificationService
import com.naposystems.napoleonchat.service.notification.SocketNotificationServiceImp
import com.naposystems.napoleonchat.service.socket.NEWSocketService
import com.naposystems.napoleonchat.service.socket.NEWSocketServiceImp
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class SocketModule {

    @Binds
    @Singleton
    abstract fun bindSocketService(socketServiceImp: NEWSocketServiceImp): NEWSocketService

    @Binds
    @Singleton
    abstract fun bindSocketNotificationService(socketNotificationServiceImp: SocketNotificationServiceImp): SocketNotificationService

}