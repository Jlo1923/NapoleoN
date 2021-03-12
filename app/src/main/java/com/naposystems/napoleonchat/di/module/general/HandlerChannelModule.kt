package com.naposystems.napoleonchat.di.module.general

import com.naposystems.napoleonchat.service.handlerChannel.HandlerChannel
import com.naposystems.napoleonchat.service.handlerChannel.HandlerChannelRepositoryImp
import com.naposystems.napoleonchat.service.handlerChannel.HandlerChannelServiceImp
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class HandlerChannelModule {

    @Binds
    @Singleton
    abstract fun bindHandlerChannelService(handlerChannelService: HandlerChannelServiceImp): HandlerChannel.Service

    @Binds
    @Singleton
    abstract fun bindHandlerChannerlRepository(handlerChannelRepository: HandlerChannelRepositoryImp): HandlerChannel.Repository

}