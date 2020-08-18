package com.naposystems.napoleonchat.di

import com.naposystems.napoleonchat.service.phoneState.PhoneStateBroadcastReceiver
import com.naposystems.napoleonchat.service.webRTCCall.WebRTCCallService
import com.naposystems.napoleonchat.utility.notificationUtils.NotificationUtils
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