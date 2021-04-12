package com.naposystems.napoleonchat.ui.multipreview.fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.naposystems.napoleonchat.databinding.FragmentMultipleAttachmentPreviewVideoBinding
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.multipreview.listeners.MultipleAttachmentPreviewListener
import com.naposystems.napoleonchat.ui.multipreview.listeners.ViewPreviewVideoControllerListener
import com.naposystems.napoleonchat.ui.multipreview.listeners.events.ViewPreviewVideoEvent
import com.naposystems.napoleonchat.utility.extensions.hide
import com.naposystems.napoleonchat.utility.extensions.show

class MultipleAttachmentPreviewVideoFragment(
    val file: MultipleAttachmentFileItem
) : Fragment(),
    ViewPreviewVideoControllerListener {

    private lateinit var binding: FragmentMultipleAttachmentPreviewVideoBinding
    private var listener: MultipleAttachmentPreviewListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMultipleAttachmentPreviewVideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun setListener(listener: MultipleAttachmentPreviewListener) {
        this.listener = listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            viewVideoController.setListener(this@MultipleAttachmentPreviewVideoFragment)
            imageButtonPlay.setOnClickListener { playVideo() }
            falseView.setOnClickListener { pauseVideo() }
        }

        configVideoView()
    }

    override fun onVideoControllerEvent(event: ViewPreviewVideoEvent) {
        when (event) {
            ViewPreviewVideoEvent.AddFlagsKeepScreen -> addFlagsKeepScreen()
            ViewPreviewVideoEvent.PauseVideo -> uiInVideoPause()
            ViewPreviewVideoEvent.PlayingVideo -> uiInVideoPlaying()
            ViewPreviewVideoEvent.RemoveFlagsKeepScreen -> removeFlagsKeepScreen()
        }
    }

    override fun onPause() {
        super.onPause()
        pauseVideo(false)
        hidePlayerOptions()
        listener?.forceShowOptions()
    }


    private fun hidePlayerOptions() = binding.apply {
        viewVideoController.hide()
        imageButtonPlay.show()
    }

    private fun showPlayerOptions() = binding.apply {
        imageButtonPlay.hide()
        viewVideoController.show()
    }

    private fun configVideoView() {

        binding.apply {
            playerView.player = viewVideoController.getExoPlayer()
            binding.playerView.useController = false
        }

        val mediaSource = buildMediaSource(file.contentUri)
        binding.viewVideoController.apply {
            mediaSource?.let { this.setMediaSource(it) }
        }

    }

    private fun buildMediaSource(uri: Uri?): MediaSource? {
        val dataSourceFactory = DefaultDataSourceFactory(context, "exoplayer")
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }

    private fun uiInVideoPlaying() = binding.root.postDelayed({
        showPlayerOptions()
        listener?.changeVisibilityOptions()
    }, 100)

    private fun uiInVideoPause() = binding.root.postDelayed({
        hidePlayerOptions()
        listener?.changeVisibilityOptions()
    }, 100)

    private fun addFlagsKeepScreen() {
        if (this.isAdded) {
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun removeFlagsKeepScreen() {
        if (this.isAdded) {
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun playVideo() = binding.viewVideoController.playVideo()

    private fun pauseVideo(notifyListener: Boolean = true) =
        binding.viewVideoController.pauseVideo(notifyListener)

}