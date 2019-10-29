package com.naposystems.pepito.app

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.facebook.stetho.Stetho
import com.naposystems.pepito.utility.LocaleHelper
import java.util.*

class NapoleonApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(LocaleHelper.setLocale(base))
        Log.i("NapoleonApplication", Locale.getDefault().country)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleHelper.setLocale(this)
    }
}