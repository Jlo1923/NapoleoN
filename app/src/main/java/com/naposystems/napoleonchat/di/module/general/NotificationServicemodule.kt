package com.naposystems.napoleonchat.di.module.general

import com.naposystems.napoleonchat.service.notificationMessage.NotificationMessagesServiceImp
import com.naposystems.napoleonchat.service.notificationMessage.NotificationMessagesService
import com.naposystems.napoleonchat.service.notificationUpload.NotificationUploadService
import com.naposystems.napoleonchat.service.notificationUpload.NotificationUploadServiceImp
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class NotificationServicemodule {

    @Binds
    @Singleton
    abstract fun bindNNotificationMessagesService(notificationMessagesService: NotificationMessagesServiceImp): NotificationMessagesService

    @Binds
    @Singleton
    abstract fun bindNotificationUploadService(notificationUploadService: NotificationUploadServiceImp): NotificationUploadService

}