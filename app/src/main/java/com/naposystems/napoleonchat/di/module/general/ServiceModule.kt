package com.naposystems.napoleonchat.di.module.general

import com.naposystems.napoleonchat.service.phoneState.PhoneStateBroadcastReceiver
import com.naposystems.napoleonchat.service.uploadService.UploadService
import com.naposystems.napoleonchat.service.webRTCCall.WebRTCCallService
import com.naposystems.napoleonchat.utility.notificationUtils.NotificationUtils
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceModule {

    @ContributesAndroidInjector
    abstract fun contributeNotificationUtils(): NotificationUtils

    @ContributesAndroidInjector
    abstract fun contributeWebRTCCallService(): WebRTCCallService

    @ContributesAndroidInjector
    abstract fun contributePhoneStateBroadcastReceiver(): PhoneStateBroadcastReceiver

    @ContributesAndroidInjector
    abstract fun contributeUploadService(): UploadService
}