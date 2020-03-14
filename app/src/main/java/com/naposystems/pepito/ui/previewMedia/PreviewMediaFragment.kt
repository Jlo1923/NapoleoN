package com.naposystems.pepito.ui.previewMedia

import android.annotation.SuppressLint
import android.content.ContentUris
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.PreviewMediaFragmentBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Utils
import timber.log.Timber

class PreviewMediaFragment : Fragment() {

    companion object {
        fun newInstance() = PreviewMediaFragment()
    }

    private val viewModel: PreviewMediaViewModel by viewModels()
    private lateinit var binding: PreviewMediaFragmentBinding

    //    private lateinit var tempFile: File
    private val args: PreviewMediaFragmentArgs by navArgs()
    private var isPlayingVideo: Boolean = false
    private var isUIVisible: Boolean = true
    private var contentUri: Uri? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 1
    private val exoplayer: SimpleExoPlayer by lazy {
        ExoPlayerFactory.newSimpleInstance(context)
    }
    private val messageAndAttachment: MessageAndAttachment by lazy {
        args.messageAndAttachment
    }
    private val animationFadeIn: Animation by lazy {
        AnimationUtils.loadAnimation(
            context!!,
            R.anim.fade_in_fast
        )
    }
    private val animationFadeOut: Animation by lazy {
        AnimationUtils.loadAnimation(
            context!!,
            R.anim.fade_out_fast
        )
    }
    private val mHandler: Handler by lazy {
        Handler()
    }
    private lateinit var mRunnable: Runnable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.preview_media_fragment,
            container,
            false
        )

        binding.message = args.messageAndAttachment

        if (args.messageAndAttachment.message.body.isEmpty()) {
            binding.containerMessageAndSeekbar.visibility = View.GONE
        }

        val firstAttachment = messageAndAttachment.attachmentList[0]

        when (firstAttachment.type) {
            Constants.AttachmentType.IMAGE.type -> {
                if (binding.viewSwitcher.currentView.id == binding.containerVideoView.id) {
                    binding.viewSwitcher.showNext()
                }
                Glide.with(binding.imageViewPreview)
                    .load(firstAttachment)
                    .into(binding.imageViewPreview)
            }
            Constants.AttachmentType.VIDEO.type -> {
                binding.containerSeekbar.visibility = View.VISIBLE
                /*binding.imageViewPreview.visibility = View.GONE

                if (binding.viewSwitcher.currentView.id == binding.imageViewPreview.id) {
                    binding.viewSwitcher.showNext()
                }*/

                contentUri = Utils.getFileUri(
                    context = context!!,
                    subFolder = Constants.NapoleonCacheDirectories.VIDEOS.folder,
                    fileName = firstAttachment.uri
                )

                /*when (firstAttachment.origin) {
                    *//*Constants.AttachmentOrigin.GALLERY.origin -> {
                        contentUri = Uri.parse(firstAttachment.uri)
                    }
                    Constants.AttachmentOrigin.CAMERA.origin -> {
                        contentUri = Uri.parse(firstAttachment.uri)
                    }
                    Constants.AttachmentOrigin.DOWNLOADED.origin -> {
                        *//**//*tempFile = Utils.createTempFileFromEncryptedFile(
                            context = context!!,
                            file = File(firstAttachment.uri),
                            extension = ".mp4"
                        )*//**//*
                        contentUri = Uri.parse(firstAttachment.uri)
                    }*//*
                }*/

                initializePlayer()

                /*binding.videoView.apply {
                    setVideoURI(contentUri)
                    requestFocus()
                    seekTo(1)

                    setOnPreparedListener {
                        binding.seekbar.max = duration
                        startVideo()
                        isPlayingVideo = !isPlayingVideo
                    }

                    setOnCompletionListener {
                        this@PreviewMediaFragment.isPlayingVideo = false
                        if (::mRunnable.isInitialized) {
                            mHandler.removeCallbacks(mRunnable)
                        }

                        binding.videoView.seekTo(1)
                        binding.seekbar.apply {
                            progress = 0
                            max = duration
                        }

                        binding.imageButtonPlay.reverseAnimation()
                        showUI()
                    }

                    mRunnable = Runnable {
                        setSeekbarProgress()

                        mHandler.postDelayed(
                            mRunnable,
                            50
                        )
                    }
                }*/
            }
        }

        binding.imageButtonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.container.setOnClickListener {
            if (isUIVisible) {
                hideUI()
            } else {
                showUI()
            }
        }

        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    exoplayer.seekTo(currentWindow, progress.toLong())
                }
                binding.textViewDuration.text = Utils.getDuration(
                    (exoplayer.duration - progress),
                    showHours = false
                )
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        binding.imageButtonPlay.setOnClickListener {
            exoplayer.playWhenReady = !isPlayingVideo
        }

        binding.executePendingBindings()

        return binding.root
    }

    private fun initializePlayer() {
        binding.videoView.player = exoplayer
        binding.videoView.useController = false
        val mediaSource = buildMediaSource(contentUri)
        exoplayer.playWhenReady = playWhenReady
        exoplayer.seekTo(currentWindow, playbackPosition)
        exoplayer.prepare(mediaSource, false, false)
        exoplayer.addListener(object : Player.EventListener {

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                when (playbackState) {
                    Player.STATE_READY -> {
                        Timber.d("STATE READY: playReadyWhen: $playWhenReady")
                        if (!::mRunnable.isInitialized) {
                            binding.seekbar.max = exoplayer.duration.toInt()
                            mRunnable = Runnable {
                                setSeekbarProgress()

                                mHandler.postDelayed(
                                    mRunnable,
                                    50
                                )
                            }

                            mHandler.postDelayed(mRunnable, 0)
                        }
                    }

                    Player.STATE_ENDED -> {
                        if (::mRunnable.isInitialized) {
                            mHandler.removeCallbacks(mRunnable)
                        }

                        binding.seekbar.apply {
                            progress = 0
                            max = exoplayer.duration.toInt()
                        }
                    }
                }
            }


            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                this@PreviewMediaFragment.isPlayingVideo = isPlaying
                if (isPlaying) {
                    binding.imageButtonPlay.playAnimation()
                    hideUI()
                } else {
                    binding.imageButtonPlay.apply {
                        reverseAnimation()
                        showUI()
                    }
                }
            }
        })
    }

    private fun buildMediaSource(uri: Uri?): MediaSource? {
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(context, "exoplayer")
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(uri)
    }

    private fun releasePlayer() {
        playWhenReady = exoplayer.playWhenReady
        playbackPosition = exoplayer.currentPosition
        currentWindow = exoplayer.currentWindowIndex
        exoplayer.release()
    }

    private fun setSeekbarProgress() {
        binding.seekbar.progress = exoplayer.currentPosition.toInt()
    }

    /*
     * Ocultamos la cabecera y el seekbar
     */
    private fun hideUI() {
        binding.imageButtonPlay.apply {
            startAnimation(animationFadeOut)
            visibility = View.GONE
        }
        binding.containerHeader.apply {
            startAnimation(animationFadeOut)
            visibility = View.GONE
        }
        binding.containerMessageAndSeekbar.apply {
            startAnimation(animationFadeOut)
            visibility = View.GONE
        }
        isUIVisible = !isUIVisible
    }

    /*
     * Mostramos la cabecera y el seekbar
     */
    private fun showUI() {
        binding.containerHeader.apply {
            startAnimation(animationFadeIn)
            visibility = View.VISIBLE
        }
        binding.containerMessageAndSeekbar.apply {
            startAnimation(animationFadeIn)
            visibility = View.VISIBLE
        }
        if (binding.imageButtonPlay.visibility == View.GONE) {
            binding.imageButtonPlay.apply {
                startAnimation(animationFadeIn)
                visibility = View.VISIBLE
            }
        }

        isUIVisible = !isUIVisible
    }

    private fun startVideo() {
        binding.imageButtonPlay.playAnimation()
        hideUI()
        exoplayer.playWhenReady = true
//        mHandler.postDelayed(mRunnable, 0)
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        binding.videoView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    override fun onDestroy() {
        super.onDestroy()
        /*if (tempFile.exists()) {
            tempFile.delete()
        }*/
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
//        hideSystemUi()
        if ((Util.SDK_INT < 24)) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }


}
