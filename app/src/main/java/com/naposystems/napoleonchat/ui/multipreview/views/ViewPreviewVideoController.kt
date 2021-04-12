package com.naposystems.napoleonchat.ui.multipreview.views

import android.content.Context
import android.os.Handler
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.naposystems.napoleonchat.databinding.ViewPreviewVideoControllerBinding
import com.naposystems.napoleonchat.ui.multipreview.listeners.ViewPreviewVideoControllerListener
import com.naposystems.napoleonchat.ui.multipreview.listeners.events.ViewPreviewVideoEvent
import timber.log.Timber

class ViewPreviewVideoController @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val exoplayer: SimpleExoPlayer by lazy { ExoPlayerFactory.newSimpleInstance(context) }
    private val mHandler: Handler by lazy { Handler() }
    private var mRunnable: Runnable? = null
    private var listener: ViewPreviewVideoControllerListener? = null

    private var notifyListener = true

    private val viewBinding: ViewPreviewVideoControllerBinding =
        ViewPreviewVideoControllerBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    fun getExoPlayer(): SimpleExoPlayer = exoplayer

    fun setListener(listener: ViewPreviewVideoControllerListener) {
        this.listener = listener
    }

    init {
        defineListeners()
    }

    private fun defineListeners() {
        seekBarListener()
        addExoPlayerListener()
    }

    private fun seekBarListener() {

        viewBinding.apply {

            seekbarProgressVideo.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        notifyListener = false
                        exoplayer.seekTo(0, progress.toLong())
                    }
                    showTextsTimer(progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

            })
        }

    }

    private fun showTextsTimer(
        progress: Int
    ) {
        val timeToComplete = (exoplayer.duration - progress) / 1000
        val timeElapsed = (progress / 1000).toLong()
        viewBinding.apply {
            textTimeToComplete.text = DateUtils.formatElapsedTime(timeToComplete)
            textTimeElapsed.text = DateUtils.formatElapsedTime(timeElapsed)
        }
    }

    fun releasePlayer() {
//        playWhenReady = exoplayer.playWhenReady
//        playbackPosition = exoplayer.currentPosition
//        currentWindow = exoplayer.currentWindowIndex
        exoplayer.release()
    }


    fun setMediaSource(media: MediaSource) {
        exoplayer.playWhenReady = false
        exoplayer.repeatMode = Player.REPEAT_MODE_ALL
        exoplayer.seekTo(0, 1)
        exoplayer.prepare(media, false, false)
    }

    private fun addExoPlayerListener() {
        exoplayer.addListener(object : Player.EventListener {

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                when (playbackState) {
                    Player.STATE_READY -> handleOnPlayerStateReady(playWhenReady)
                    Player.STATE_ENDED -> handlePlayerStateEnded()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)

                if (isPlaying) {
                    if (notifyListener) listener?.onVideoControllerEvent(
                        ViewPreviewVideoEvent.PlayingVideo
                    )
                } else {
                    if (notifyListener) listener?.onVideoControllerEvent(
                        ViewPreviewVideoEvent.PauseVideo
                    )
                }
            }
        })
    }

    private fun handlePlayerStateEnded() {
        if (mRunnable != null) {
            mHandler.removeCallbacks(mRunnable!!)
            mRunnable = null
        }

        viewBinding.seekbarProgressVideo.apply {
            progress = 0
            max = exoplayer.duration.toInt()
        }

        //sentMessageReaded(false)

        listener?.onVideoControllerEvent(ViewPreviewVideoEvent.RemoveFlagsKeepScreen)
    }

    private fun handleOnPlayerStateReady(playWhenReady: Boolean) {
        Timber.d("STATE READY: playReadyWhen: $playWhenReady")
        if (mRunnable == null) {
            viewBinding.seekbarProgressVideo.max = exoplayer.duration.toInt()
            mRunnable = Runnable {
                setSeekBarProgress()
                mRunnable?.let { mHandler.postDelayed(it, 50) }
            }
            mRunnable?.let { mHandler.postDelayed(it, 0) }
        }
        listener?.onVideoControllerEvent(ViewPreviewVideoEvent.AddFlagsKeepScreen)
    }

    private fun setSeekBarProgress() {
        viewBinding.seekbarProgressVideo.progress = exoplayer.currentPosition.toInt()
    }

    fun playVideo() {
        notifyListener = true
        exoplayer.playWhenReady = true
    }

    fun pauseVideo(notifyListener: Boolean) {
        this.notifyListener = notifyListener
        exoplayer.playWhenReady = false
    }

    fun pauseExoPlayer() {
        exoplayer.apply {
            playWhenReady = false
            playbackState
        }
    }


}