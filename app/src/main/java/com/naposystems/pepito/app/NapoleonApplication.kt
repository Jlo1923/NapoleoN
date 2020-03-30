package com.naposystems.pepito.app

import androidx.core.provider.FontRequest
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import com.crashlytics.android.Crashlytics
import com.facebook.stetho.Stetho
import com.giphy.sdk.ui.Giphy
import com.naposystems.pepito.R
import com.naposystems.pepito.di.DaggerApplicationComponent
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.emojiManager.EmojiManager
import com.naposystems.pepito.webService.socket.SocketService
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import io.fabric.sdk.android.Fabric
import timber.log.Timber
import javax.inject.Inject

class NapoleonApplication : DaggerApplication() {

    companion object {
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
        EmojiManager.instance.install()
        configEmojiCompat()
        Giphy.configure(this, Constants.GIPHY_API_KEY)
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