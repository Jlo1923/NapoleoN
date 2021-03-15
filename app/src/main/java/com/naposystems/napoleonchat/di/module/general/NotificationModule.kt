package com.naposystems.napoleonchat.di.module.general

import android.content.Context
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.service.handlerNotificationChannel.HandlerNotificationChannel
import com.naposystems.napoleonchat.service.notificationMessage.OLD_NotificationService
import com.naposystems.napoleonchat.service.socketMessage.SocketMessageService
import com.naposystems.napoleonchat.service.syncManager.SyncManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class NotificationModule {
    @Provides
    @Singleton
    fun providesNotificationService(
        context: Context,
        syncManager: SyncManager,
        handlerNotificationChannelService: HandlerNotificationChannel.Service,
        cryptoMessage: CryptoMessage,
        socketMessageService: SocketMessageService
    ): OLD_NotificationService {
        return OLD_NotificationService(
            context,
            syncManager,
            handlerNotificationChannelService,
            cryptoMessage,
            socketMessageService
        )
    }

}