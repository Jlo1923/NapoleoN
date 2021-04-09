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

    enum class ActionPlay {
        ACTION_NONE,
        ACTION_PLAY_RINGTONE,
        ACTION_PLAY_ENDTONE,
        ACTION_PLAY_BUSY_TONE,
        ACTION_PLAY_RING_BACK
    }

    lateinit var mediaPlayer: MediaPlayer

    var currentlyAction: ActionPlay = ActionPlay.ACTION_NONE

    private var stringResource: String = "android.resource://" + context.packageName + "/"

    private val vibratePattern = longArrayOf(0, 400, 1000, 600, 1000, 800, 1000, 1000)

    private val vibrator: Vibrator? by lazy {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun playRingtone() {

        if (::mediaPlayer.isInitialized)
            Timber.d("RINGTONE: playRingtone mediaPlayer. $mediaPlayer")
        else
            Timber.d("RINGTONE: playRingtone mediaPlayer. NO INICIALIZADO")

        playMedia(
            Settings.System.DEFAULT_RINGTONE_URI,
            isLooping = true,
            needVibrate = true,
            ActionPlay.ACTION_PLAY_RINGTONE
        )
    }

    override fun playEndTone() {

        if (::mediaPlayer.isInitialized)
            Timber.d("RINGTONE: playEndTone mediaPlayer. $mediaPlayer")
        else
            Timber.d("RINGTONE: playEndTone mediaPlayer. NO INICIALIZADO")

        playMedia(
            Uri.parse(stringResource + R.raw.end_call_tone),
            isLooping = false,
            needVibrate = false,
            ActionPlay.ACTION_PLAY_RING_BACK
        )
    }

    override fun playRingBack() {

        if (::mediaPlayer.isInitialized)
            Timber.d("RINGTONE: playRingBack mediaPlayer. $mediaPlayer")
        else
            Timber.d("RINGTONE: playRingBack mediaPlayer. NO INICIALIZADO")

        playMedia(
            Uri.parse(stringResource + R.raw.ringback_tone),
            isLooping = true,
            needVibrate = false,
            ActionPlay.ACTION_PLAY_RING_BACK
        )
    }

    override fun playBusyTone() {

        if (::mediaPlayer.isInitialized)
            Timber.d("RINGTONE: playBusyTone mediaPlayer. $mediaPlayer")
        else
            Timber.d("RINGTONE: playBusyTone mediaPlayer. NO INICIALIZADO")

        playMedia(
            Uri.parse(stringResource + R.raw.busy_tone),
            isLooping = true,
            needVibrate = false,
            ActionPlay.ACTION_PLAY_BUSY_TONE
        )
    }

    override fun stopRingtone() {

        Timber.d("RINGTONE: stopRingtone")
        currentlyAction = ActionPlay.ACTION_NONE
        stopMedia()
    }

    private fun playMedia(
        uriSound: Uri,
        isLooping: Boolean = false,
        needVibrate: Boolean = false,
        actionPlay: ActionPlay
    ) {

        try {

            if (::mediaPlayer.isInitialized.not() || mediaPlayer == null)
                mediaPlayer = MediaPlayer()

            if (currentlyAction != actionPlay) {

                currentlyAction = actionPlay

                stopMedia()

                mediaPlayer = mediaPlayer.apply {
//                    setAudioAttributes(
//                        AudioAttributes
//                            .Builder()
//                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
//                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                            .build()
//                    )
                    setDataSource(
                        context,
                        uriSound
                    )
                    this.isLooping = isLooping
                    prepare()
                    start()
                }

                Timber.d("RINGTONE: playMedia mediaPlayer. $mediaPlayer uriSound $uriSound")

                if (needVibrate)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val effect = VibrationEffect.createWaveform(vibratePattern, 0)
                        vibrator?.vibrate(effect)
                    } else {
                        vibrator?.vibrate(vibratePattern, 0)
                    }

            }

        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun stopMedia() {
        try {

            if (::mediaPlayer.isInitialized)
                if (mediaPlayer.isPlaying) {
                    Timber.d("RINGTONE: stopMedia mediaPlayer. $mediaPlayer ")
                    mediaPlayer.apply {
                        stop()
                        reset()
                    }
                }

            vibrator?.cancel()

        } catch (e: Exception) {
            Timber.e(e)
        }
    }

}