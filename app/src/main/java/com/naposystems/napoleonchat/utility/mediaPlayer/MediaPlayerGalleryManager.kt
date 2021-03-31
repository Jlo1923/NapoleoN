package com.naposystems.napoleonchat.utility.mediaPlayer

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.naposystems.napoleonchat.R
import timber.log.Timber
import javax.inject.Inject

class MediaPlayerGalleryManager @Inject constructor(
    private val context: Context
) {

    private var mediaPlayer: SimpleExoPlayer? = null
    private var mImageButtonPlay: ImageView? = null
    private var mPreviousAudioId: String? = null
    private var currentAudioId: String = ""
    private var currentAudioUri: Uri? = null
    private var mListener: Listener? = null

    private val loadControl: LoadControl = DefaultLoadControl.Builder().setBufferDurationsMs(
        Int.MAX_VALUE,
        Int.MAX_VALUE,
        Int.MAX_VALUE,
        Int.MAX_VALUE
    ).createDefaultLoadControl()

    interface Listener {
        fun onErrorPlayingAudio()
        fun onCompleteAudio(messageId: String)
    }

    fun setAudioId(audioId: String) {
        if (mPreviousAudioId != audioId) {
            if (mediaPlayer?.isPlaying == true) {
                changeIconPlayPause(R.drawable.ic_baseline_play_circle)
            }
        }
        this.currentAudioId = audioId
    }

    fun setAudioUri(uri: Uri?) {
        this.currentAudioUri = uri
    }

    fun playAudio() {
        if (mPreviousAudioId == currentAudioId) {
            if (mediaPlayer != null) {
                if (mediaPlayer?.isPlaying == true) {
                    changeIconPlayPause(R.drawable.ic_baseline_play_circle)
                } else {
                    changeIconPlayPause(R.drawable.ic_baseline_pause_circle)
                }
            } else {
                changeIconPlayPause(R.drawable.ic_baseline_pause_circle)
            }
            mediaPlayer?.playWhenReady = !mediaPlayer!!.isPlaying
        } else {
            mPreviousAudioId = currentAudioId

            mediaPlayer?.stop()
            mediaPlayer?.release()

            mediaPlayer = ExoPlayerFactory.newSimpleInstance(
                context,
                DefaultRenderersFactory(context),
                DefaultTrackSelector(),
                loadControl
            )

            with(mediaPlayer!!) {

                currentAudioUri?.let { uri ->
                    prepare(buildMediaSource(uri))
                }

                playWhenReady = true

                addListener(object : Player.EventListener {
                    var started = false
                    override fun onPlayerStateChanged(
                        playWhenReady: Boolean,
                        playbackState: Int
                    ) {
                        when (playbackState) {
                            Player.STATE_READY -> {
                                if (mediaPlayer == null) return

                                if (started) {
                                    return
                                }
                                started = true
                                changeIconPlayPause(R.drawable.ic_baseline_pause_circle)
                            }

                            Player.STATE_ENDED -> {
                                changeIconPlayPause(R.drawable.ic_baseline_play_circle)
                                resetMediaPlayer()
                                mListener?.onCompleteAudio(
                                    currentAudioId
                                )
                            }
                        }
                    }

                    override fun onPlayerError(error: ExoPlaybackException?) {
                        Timber.w("Conver MediaPlayer Error: $error")
                        resetMediaPlayer()
                        mListener?.onErrorPlayingAudio()
                    }
                })
            }
        }
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    fun setImageButtonPlay(imageButtonPlay: ImageView) {
        this.mImageButtonPlay = imageButtonPlay
    }

    fun getAudioId() = this.currentAudioId

    fun isPlaying() = mediaPlayer?.isPlaying ?: false

    fun resetMediaPlayer() {
        changeIconPlayPause(R.drawable.ic_baseline_play_circle)
        mImageButtonPlay = null
        mPreviousAudioId = null
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(context, "exoplayer")
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(uri)
    }

    private fun changeIconPlayPause(drawable: Int) {
        mImageButtonPlay?.setImageDrawable(
            context.resources.getDrawable(
                drawable, context.theme
            )
        )
    }
}