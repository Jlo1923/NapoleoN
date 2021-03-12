package com.naposystems.napoleonchat.di.module.general

import android.content.Context
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.service.notification.NEW_NotificationServiceImp
import com.naposystems.napoleonchat.service.notification.NotificationService
import com.naposystems.napoleonchat.service.notification.OLD_NotificationService
import com.naposystems.napoleonchat.service.notification.SocketNotificationService
import com.naposystems.napoleonchat.service.socket.SocketService
import com.naposystems.napoleonchat.service.syncManager.SyncManager
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
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
        cryptoMessage: CryptoMessage,
        socketService: SocketService
    ): OLD_NotificationService {
        return OLD_NotificationService(
            context,
            syncManager,
            cryptoMessage,
            socketService
        )
    }

    @Provides
    @Singleton
    fun providesNewNotificationService(
        context: Context,
        socketNotificationService: SocketNotificationService,
        sharedPreferencesManager: SharedPreferencesManager,
        syncManager: SyncManager,
        cryptoMessage: CryptoMessage
    ): NotificationService {
        return NEW_NotificationServiceImp(
            context,
            socketNotificationService,
            sharedPreferencesManager,
            syncManager,
            cryptoMessage
        )
    }

}