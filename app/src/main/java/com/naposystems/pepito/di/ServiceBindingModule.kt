package com.naposystems.pepito.di

import com.naposystems.pepito.service.phoneState.PhoneStateBroadcastReceiver
import com.naposystems.pepito.service.webRTCCall.WebRTCCallService
import com.naposystems.pepito.utility.notificationUtils.NotificationUtils
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBindingModule {

    @ContributesAndroidInjector
    abstract fun bindNotificationUtils(): NotificationUtils

    @ContributesAndroidInjector
    abstract fun bindWebRTCCallService(): WebRTCCallService

    @ContributesAndroidInjector
    abstract fun bindPhoneStateBroadcastReceiver(): PhoneStateBroadcastReceiver
}