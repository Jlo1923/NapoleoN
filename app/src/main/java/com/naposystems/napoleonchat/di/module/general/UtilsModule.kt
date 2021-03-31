package com.naposystems.napoleonchat.di.module.general

import com.naposystems.napoleonchat.utils.handlerDialog.HandlerDialog
import com.naposystems.napoleonchat.utils.handlerDialog.HandlerDialogImp
import com.naposystems.napoleonchat.utils.handlerNotificationChannel.HandlerNotificationChannel
import com.naposystems.napoleonchat.utils.handlerNotificationChannel.HandlerNotificationChannelImp
import com.naposystems.napoleonchat.utils.handlerNotificationChannel.HandlerNotificationChannelRepository
import com.naposystems.napoleonchat.utils.handlerNotificationChannel.HandlerNotificationChannelRepositoryImp
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class UtilsModule {

    @Binds
    @Singleton
    abstract fun bindHandlerChannel(handlerNotificationChannel: HandlerNotificationChannelImp): HandlerNotificationChannel

    @Binds
    @Singleton
    abstract fun bindHandlerChannelRepository(repository: HandlerNotificationChannelRepositoryImp): HandlerNotificationChannelRepository

    @Binds
    abstract fun bindHandlerDialog(handlerDialog: HandlerDialogImp): HandlerDialog

}