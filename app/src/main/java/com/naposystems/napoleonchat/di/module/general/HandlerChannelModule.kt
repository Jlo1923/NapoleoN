package com.naposystems.napoleonchat.di.module.general

import com.naposystems.napoleonchat.service.handlerNotificationChannel.HandlerNotificationChannel
import com.naposystems.napoleonchat.service.handlerNotificationChannel.HandlerNotificationChannelRepositoryImp
import com.naposystems.napoleonchat.service.handlerNotificationChannel.HandlerNotificationChannelServiceImp
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class HandlerChannelModule {

    @Binds
    @Singleton
    abstract fun bindHandlerChannelService(handlerNotificationChannelService: HandlerNotificationChannelServiceImp): HandlerNotificationChannel.Service

    @Binds
    @Singleton
    abstract fun bindHandlerChannerlRepository(handlerNotificationChannelRepository: HandlerNotificationChannelRepositoryImp): HandlerNotificationChannel.Repository

}