package com.naposystems.napoleonchat.di.module.general

import android.content.Context
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.service.handlerChannel.HandlerChannel
import com.naposystems.napoleonchat.service.notification.OLD_NotificationService
import com.naposystems.napoleonchat.service.socket.NEWSocketService
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
        handlerChannelService: HandlerChannel.Service,
        cryptoMessage: CryptoMessage,
        newSocketService: NEWSocketService
    ): OLD_NotificationService {
        return OLD_NotificationService(
            context,
            syncManager,
            handlerChannelService,
            cryptoMessage,
            newSocketService
        )
    }

}