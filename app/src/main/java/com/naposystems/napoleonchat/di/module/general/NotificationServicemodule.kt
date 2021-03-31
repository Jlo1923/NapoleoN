package com.naposystems.napoleonchat.di.module.general

import com.naposystems.napoleonchat.service.notificationClient.*
import com.naposystems.napoleonchat.service.notificationUploadClient.NotificationUploadClient
import com.naposystems.napoleonchat.service.notificationUploadClient.NotificationUploadClientImp
import com.naposystems.napoleonchat.utils.handlerMediPlayer.HandlerMediaPlayerNotification
import com.naposystems.napoleonchat.utils.handlerMediPlayer.HandlerMediaPlayerNotificationImp
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class NotificationServicemodule {

    @Binds
    @Singleton
    abstract fun bindNotificationClient(notificationClient: NotificationClientImp): NotificationClient

    @Binds
    @Singleton
    abstract fun bindHandlerNotification(handlerNotification: HandlerNotificationImp): HandlerNotification

    @Binds
    @Singleton
    abstract fun bindHandlerNotificationMessage(handlerNotificationMessage: HandlerNotificationMessageImp): HandlerNotificationMessage

    @Binds
    @Singleton
    abstract fun bindHandlerNotificationCall(handlerNotificationCall: HandlerNotificationCallImp): HandlerNotificationCall

    @Binds
    @Singleton
    abstract fun bindNotificationUploadClient(notificationUploadClient: NotificationUploadClientImp): NotificationUploadClient

    @Binds
    @Singleton
    abstract fun bindHandlerMediaPlayer(handlerMediaPlayerNotification: HandlerMediaPlayerNotificationImp): HandlerMediaPlayerNotification

}