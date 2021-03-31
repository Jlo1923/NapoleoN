package com.naposystems.napoleonchat.di.module.general

import com.naposystems.napoleonchat.service.firebase.NapoleonFirebaseMessagingService
import com.naposystems.napoleonchat.service.multiattachment.MultipleUploadService
import com.naposystems.napoleonchat.service.phoneState.PhoneStateBroadcastReceiver
import com.naposystems.napoleonchat.service.uploadService.UploadService
import com.naposystems.napoleonchat.webRTC.service.WebRTCService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceModule {

    @ContributesAndroidInjector
    abstract fun contributeWebRTCService(): WebRTCService

    @ContributesAndroidInjector
    abstract fun contributePhoneStateBroadcastReceiver(): PhoneStateBroadcastReceiver

    @ContributesAndroidInjector
    abstract fun contributeUploadService(): UploadService

    @ContributesAndroidInjector
    abstract fun contributeMultipleUploadService(): MultipleUploadService

    @ContributesAndroidInjector
    abstract fun contributeNapoleonFirebaseMessagingService(): NapoleonFirebaseMessagingService

}