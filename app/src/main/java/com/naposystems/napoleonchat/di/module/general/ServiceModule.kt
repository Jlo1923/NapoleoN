package com.naposystems.napoleonchat.di.module.general

import com.naposystems.napoleonchat.service.firebase.NapoleonFirebaseMessagingService
import com.naposystems.napoleonchat.service.phoneState.PhoneStateBroadcastReceiver
import com.naposystems.napoleonchat.service.uploadService.UploadService
import com.naposystems.napoleonchat.service.webRTCCall.WebRTCCallService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceModule {
//
//    @ContributesAndroidInjector
//    abstract fun contributeNotificationService(): NotificationService
//
//    @ContributesAndroidInjector
//    abstract fun contributeNewNotificationService(): NEW_NotificationService

    @ContributesAndroidInjector
    abstract fun contributeWebRTCCallService(): WebRTCCallService

    @ContributesAndroidInjector
    abstract fun contributePhoneStateBroadcastReceiver(): PhoneStateBroadcastReceiver

    @ContributesAndroidInjector
    abstract fun contributeUploadService(): UploadService

    @ContributesAndroidInjector
    abstract fun contributeNapoleonFirebaseMessagingService(): NapoleonFirebaseMessagingService

}