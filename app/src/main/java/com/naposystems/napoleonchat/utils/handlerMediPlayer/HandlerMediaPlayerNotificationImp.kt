package com.naposystems.napoleonchat.utils.handlerMediPlayer

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import com.naposystems.napoleonchat.R
import timber.log.Timber
import javax.inject.Inject

class HandlerMediaPlayerNotificationImp
@Inject constructor(
    private val context: Context
) : HandlerMediaPlayerNotification {

    lateinit var mediaPlayer: MediaPlayer

    private var stringResource: String = "android.resource://" + context.packageName + "/"

    private val vibratePattern = longArrayOf(0, 400, 1000, 600, 1000, 800, 1000, 1000)

    private val vibrator: Vibrator? by lazy {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun playRingtone() {

        Timber.d("MEDIAPLAYER: playRingtone")
        playMedia(Settings.System.DEFAULT_RINGTONE_URI, isLooping = true, needVibrate = true)
    }

    override fun playEndTone() {
        Timber.d("MEDIAPLAYER: playEndTone")
        playMedia(Uri.parse(stringResource + R.raw.end_call_tone))
    }

    override fun playRingBack() {
        Timber.d("MEDIAPLAYER: playRingBack")
        playMedia(Uri.parse(stringResource + R.raw.ringback_tone))
    }

    override fun playBusyTone() {
        Timber.d("MEDIAPLAYER: playBusyTone")
        playMedia(Uri.parse(stringResource + R.raw.busy_tone), isLooping = true)
    }

    private fun playMedia(uriSound: Uri, isLooping: Boolean = false, needVibrate: Boolean = false) {

        try {

            stopMedia()

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes
                        .Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(
                    context,
                    uriSound
                )
                this.isLooping = isLooping
                prepare()
                start()
            }

            if (needVibrate)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(vibratePattern, 0)
                    vibrator?.vibrate(effect)
                } else {
                    vibrator?.vibrate(vibratePattern, 0)
                }

        } catch (e: Exception) {
            Timber.e(e)
        }
    }


    override fun stopMedia() {
        try {

            if (::mediaPlayer.isInitialized) {

                mediaPlayer.stop()

                vibrator?.cancel()
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

}