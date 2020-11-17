package com.naposystems.napoleonchat.utility.audioManagerCompat

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.SoundPool
import android.os.Build
import androidx.annotation.RequiresApi
import timber.log.Timber

abstract class AudioManagerCompat private constructor(context: Context) {
    protected val audioManager: AudioManager = (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
    protected val onAudioFocusChangeListener =
        OnAudioFocusChangeListener { focusChange: Int ->
            Timber.i(
                "onAudioFocusChangeListener: $focusChange"
            )
        }

    abstract fun createSoundPool(): SoundPool?
    abstract fun requestCallAudioFocus()
    abstract fun abandonCallAudioFocus()

    @RequiresApi(26)
    private class Api26AudioManagerCompat constructor(context: Context) :
        AudioManagerCompat(context) {
        private var audioFocusRequest: AudioFocusRequest? = null
        override fun createSoundPool(): SoundPool {
            return SoundPool.Builder()
                .setAudioAttributes(AUDIO_ATTRIBUTES)
                .setMaxStreams(1)
                .build()
        }

        override fun requestCallAudioFocus() {
            if (audioFocusRequest != null) {
                Timber.w(
                    "Already requested audio focus. Ignoring..."
                )
                return
            }
            audioFocusRequest =
                AudioFocusRequest.Builder(AUDIOFOCUS_GAIN)
                    .setAudioAttributes(AUDIO_ATTRIBUTES)
                    .setOnAudioFocusChangeListener(onAudioFocusChangeListener)
                    .build()
            val result = audioManager.requestAudioFocus(audioFocusRequest!!)
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Timber.w(
                    "Audio focus not granted. Result code: $result"
                )
            }
        }

        override fun abandonCallAudioFocus() {
            if (audioFocusRequest == null) {
                Timber.w(
                    "Don't currently have audio focus. Ignoring..."
                )
                return
            }
            val result = audioManager.abandonAudioFocusRequest(audioFocusRequest!!)
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Timber.w(
                    "Audio focus abandon failed. Result code: $result"
                )
            }
            audioFocusRequest = null
        }

        companion object {
            private val AUDIO_ATTRIBUTES =
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .build()
        }
    }

    @RequiresApi(21)
    private class Api21AudioManagerCompat constructor(context: Context) :
        AudioManagerCompat(context) {

        override fun createSoundPool(): SoundPool {
            return SoundPool.Builder()
                .setAudioAttributes(AUDIO_ATTRIBUTES)
                .setMaxStreams(1)
                .build()
        }

        override fun requestCallAudioFocus() {
            val result = audioManager.requestAudioFocus(
                onAudioFocusChangeListener,
                AudioManager.STREAM_VOICE_CALL,
                AUDIOFOCUS_GAIN
            )
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Timber.w(
                    "Audio focus not granted. Result code: $result"
                )
            }
        }

        override fun abandonCallAudioFocus() {
            val result = audioManager.abandonAudioFocus(onAudioFocusChangeListener)
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Timber.w(
                    "Audio focus abandon failed. Result code: $result"
                )
            }
        }

        companion object {
            private val AUDIO_ATTRIBUTES =
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setLegacyStreamType(AudioManager.STREAM_VOICE_CALL)
                    .build()
        }
    }

    companion object {
        private const val AUDIOFOCUS_GAIN = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
        fun create(context: Context): AudioManagerCompat {
            return when {
                Build.VERSION.SDK_INT >= 26 -> {
                    Api26AudioManagerCompat(context)
                }
                else -> {
                    Api21AudioManagerCompat(context)
                }
            }
        }
    }

}