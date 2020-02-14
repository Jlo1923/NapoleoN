package com.naposystems.pepito.app

import com.facebook.stetho.Stetho
import com.naposystems.pepito.di.DaggerApplicationComponent
import com.naposystems.pepito.webService.socket.SocketService
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import timber.log.Timber
import javax.inject.Inject

class NapoleonApplication : DaggerApplication() {

    @Inject
    lateinit var socketService: SocketService

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerApplicationComponent.builder().create(this).build()
    }

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
        Timber.plant(Timber.DebugTree())
    }
}