package com.naposystems.napoleonchat.di.module.general

import android.content.Context
import android.media.RingtoneManager
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.service.notification.NEW_NotificationServiceImp
import com.naposystems.napoleonchat.service.notification.NotificationService
import com.naposystems.napoleonchat.subscription.BillingClientLifecycle
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
abstract class NewNotificationServicemodule {


    @Binds
    @Singleton
    abstract fun bindNewNotificationService(newNotificationService: NEW_NotificationServiceImp): NotificationService
}