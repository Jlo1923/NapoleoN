package com.naposystems.napoleonchat.utils.handlerMediPlayer

import android.content.Context
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
        ACTION_PLAY_RING_TONE,
        ACTION_PLAY_END_TONE,
        ACTION_PLAY_BUSY_TONE,
        ACTION_PLAY_BACK_TONE
    }

    var mediaPlayer: MediaPlayer? = null

    var currentlyAction: ActionPlay = ActionPlay.ACTION_NONE

    private var stringResource: String = "android.resource://" + context.packageName + "/"

    private val vibratePattern = longArrayOf(0, 400, 1000, 600, 1000, 800, 1000, 1000)

    private val vibrator: Vibrator? by lazy {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun playRingTone() {
        if (mediaPlayer != null)
            Timber.d("RINGTONE: playRingtone mediaPlayer. $mediaPlayer")
        else
            Timber.d("RINGTONE: playRingtone mediaPlayer. NO INICIALIZADO")

        playMedia(
            Settings.System.DEFAULT_RINGTONE_URI,
            isLooping = true,
            needVibrate = true,
            ActionPlay.ACTION_PLAY_RING_TONE
        )
    }

    override fun playEndTone() {
        if (mediaPlayer != null)
            Timber.d("RINGTONE: playEndTone mediaPlayer. $mediaPlayer")
        else
            Timber.d("RINGTONE: playEndTone mediaPlayer. NO INICIALIZADO")

        playMedia(
            Uri.parse(stringResource + R.raw.end_call_tone),
            isLooping = false,
            needVibrate = false,
            ActionPlay.ACTION_PLAY_END_TONE
        )
    }

    override fun playBackTone() {
        if (mediaPlayer != null)
            Timber.d("RINGTONE: playRingBack mediaPlayer. $mediaPlayer")
        else
            Timber.d("RINGTONE: playRingBack mediaPlayer. NO INICIALIZADO")

        playMedia(
            Uri.parse(stringResource + R.raw.ringback_tone),
            isLooping = true,
            needVibrate = false,
            ActionPlay.ACTION_PLAY_BACK_TONE
        )
    }

    override fun playBusyTone() {
        if (mediaPlayer != null)
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

    override fun stopTone() {
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

            if (currentlyAction != actionPlay) {

                currentlyAction = actionPlay

                stopMedia()

                if (mediaPlayer == null)
                    mediaPlayer = MediaPlayer()

                mediaPlayer?.apply {
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
            if (mediaPlayer != null) {
                if (mediaPlayer!!.isPlaying) {
                    Timber.d("RINGTONE: stopMedia mediaPlayer. $mediaPlayer ")
                    mediaPlayer?.apply {
                        stop()
                        reset()
                    }
                }
            }
            mediaPlayer = null
            vibrator?.cancel()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

}