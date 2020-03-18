package com.naposystems.pepito.app

import androidx.core.provider.FontRequest
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import com.crashlytics.android.Crashlytics
import com.facebook.stetho.Stetho
import com.naposystems.pepito.R
import com.naposystems.pepito.di.DaggerApplicationComponent
import com.naposystems.pepito.webService.socket.SocketService
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import io.fabric.sdk.android.Fabric
import timber.log.Timber
import javax.inject.Inject

class NapoleonApplication : DaggerApplication() {

    companion object {
        /** Change this to `false` when you want to use the downloadable Emoji font.  */
        private const val USE_BUNDLED_EMOJI = true
    }

    @Inject
    lateinit var socketService: SocketService

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerApplicationComponent.builder().create(this).build()
    }

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
        Timber.plant(Timber.DebugTree())
        Fabric.with(this, Crashlytics())
        EmojiManager.install(IosEmojiProvider())
        configEmojiCompat()
    }

    private fun configEmojiCompat() {
        val config: EmojiCompat.Config
        if (USE_BUNDLED_EMOJI) {
            // Use the bundled font for EmojiCompat
            config = BundledEmojiCompatConfig(applicationContext)
        } else {
            // Use a downloadable font for EmojiCompat
            val fontRequest = FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "Noto Color Emoji Compat",
                R.array.com_google_android_gms_fonts_certs
            )
            config = FontRequestEmojiCompatConfig(applicationContext, fontRequest)
                .setReplaceAll(true)
                .registerInitCallback(object : EmojiCompat.InitCallback() {
                    override fun onInitialized() {
                        Timber.i("EmojiCompat initialized")
                    }

                    override fun onFailed(throwable: Throwable?) {
                        Timber.e("EmojiCompat initialization failed: \n $throwable")
                    }
                })
        }
        EmojiCompat.init(config)
    }
}