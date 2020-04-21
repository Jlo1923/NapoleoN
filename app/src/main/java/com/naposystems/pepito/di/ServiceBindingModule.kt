package com.naposystems.pepito.di

import com.naposystems.pepito.utility.notificationUtils.NotificationUtils
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBindingModule {

    @ContributesAndroidInjector
    abstract fun bindNotificationUtils(): NotificationUtils
}