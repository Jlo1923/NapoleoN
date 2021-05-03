package com.naposystems.napoleonchat.app

import android.app.NotificationManager
import android.content.Context
import androidx.core.provider.FontRequest
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.facebook.stetho.Stetho
import com.giphy.sdk.ui.Giphy
import com.google.android.libraries.places.api.Places
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.di.DaggerApplicationComponent
import com.naposystems.napoleonchat.model.CallModel
import com.naposystems.napoleonchat.service.socketClient.SocketClient
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.StatusCallEnum
import com.naposystems.napoleonchat.utility.emojiManager.EmojiManager
import com.naposystems.napoleonchat.utility.isNoCall
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import timber.log.Timber
import javax.inject.Inject

class NapoleonApplication : DaggerApplication(), DefaultLifecycleObserver {

    companion object {
        private const val USE_BUNDLED_EMOJI = true

        var isVisible: Boolean = false

        var statusCall: StatusCallEnum = StatusCallEnum.STATUS_NO_CALL

        var isShowingCallActivity: Boolean = false

        var callInfoModel: CallModel? =  null

        var currentConversationContactId: Int = 0

//        var currentCallContactId: Int = 0

    }

    @Inject
    lateinit var socketClient: SocketClient

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
        DaggerApplicationComponent.builder().create(this).build()

    override fun onCreate() {
        super<DaggerApplication>.onCreate()

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
            Timber.plant(Timber.DebugTree())
        }

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

        EmojiManager.instance.install()

        configEmojiCompat()

        Giphy.configure(this, Constants.GIPHY_API_KEY)

        Places.initialize(this, "AIzaSyDcAkhqRS4BO-BoKM89HiXuR4ruLr8fj1w")

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.cancelAll()
    }

    override fun onStart(owner: LifecycleOwner) {

        Timber.d("*NotificationTest: onStart")

        isVisible = true

    }

    override fun onResume(owner: LifecycleOwner) {

        super.onResume(owner)

        isVisible = true

    }

    override fun onPause(owner: LifecycleOwner) {

        super.onPause(owner)

        isVisible = false

    }

    override fun onStop(owner: LifecycleOwner) {

        Timber.d("LLAMADA PASO 3: ONSTOP APPLICATION")
        if (NapoleonApplication.statusCall.isNoCall())
            socketClient.disconnectSocket()

        isVisible = false

    }

    override fun onDestroy(owner: LifecycleOwner) {
        Timber.d("LLAMADA PASO 3: ONDESTROY APPLICATION")
        if (NapoleonApplication.statusCall.isNoCall())
            socketClient.disconnectSocket()

        super.onDestroy(owner)

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