package com.naposystems.pepito.app

import android.content.Context
import android.content.res.Configuration
import com.facebook.stetho.Stetho
import com.naposystems.pepito.di.DaggerApplicationComponent
import com.naposystems.pepito.utility.LocaleHelper
import com.naposystems.pepito.webService.SocketService
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

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(LocaleHelper.setLocale(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleHelper.setLocale(this)
    }
}