package com.naposystems.napoleonchat.di.module.general

import com.naposystems.napoleonchat.service.handlerNotificationChannel.HandlerNotificationChannel
import com.naposystems.napoleonchat.service.handlerNotificationChannel.HandlerChannelRepositoryImp
import com.naposystems.napoleonchat.service.handlerNotificationChannel.HandlerChannelServiceImp
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class HandlerChannelModule {

    @Binds
    @Singleton
    abstract fun bindHandlerChannelService(handlerChannelService: HandlerChannelServiceImp): HandlerNotificationChannel.Service

    @Binds
    @Singleton
    abstract fun bindHandlerChannerlRepository(handlerChannelRepository: HandlerChannelRepositoryImp): HandlerNotificationChannel.Repository

}