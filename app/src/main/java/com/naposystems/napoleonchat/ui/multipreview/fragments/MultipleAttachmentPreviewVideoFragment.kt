package com.naposystems.napoleonchat.ui.multipreview.fragments

import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.FragmentMultipleAttachmentPreviewVideoBinding
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.ui.baseFragment.BaseFragment
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.multipreview.listeners.MultipleAttachmentPreviewListener
import com.naposystems.napoleonchat.ui.multipreview.listeners.ViewPreviewVideoControllerListener
import com.naposystems.napoleonchat.ui.multipreview.listeners.events.ViewPreviewVideoEvent
import com.naposystems.napoleonchat.ui.multipreview.viewmodels.MultipleAttachmentPreviewItemViewModel
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.extensions.hide
import com.naposystems.napoleonchat.utility.extensions.hideViews
import com.naposystems.napoleonchat.utility.extensions.show
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MultipleAttachmentPreviewVideoFragment(
    val file: MultipleAttachmentFileItem,
    val position: Int
) : BaseFragment(),
    ViewPreviewVideoControllerListener {

    @Inject
    override lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: MultipleAttachmentPreviewItemViewModel by viewModels {
        viewModelFactory
    }

    private lateinit var binding: FragmentMultipleAttachmentPreviewVideoBinding
    private var listener: MultipleAttachmentPreviewListener? = null

    private var countDownTimer: CountDownTimer? = null

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
            ViewPreviewVideoEvent.VideoEnded -> uiInVideoInit()
        }
    }

    private fun uiInVideoInit() {
        removeFlagsKeepScreen()
        configVideoView()
    }

    override fun onPause() {
        super.onPause()
        pauseVideo(false)
        hidePlayerOptions()
        listener?.forceShowOptions()
    }

    override fun onStart() {
        super.onStart()
        file.messageAndAttachment?.let {
            viewModel.setAttachmentAndLaunchLiveData(it.attachment.webId)
            bindViewModel()
        }
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
        listener?.let {
            it.changeVisibilityOptions()
            it.markAttachmentAsRead(file)
        }
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

    private fun bindViewModel() {
        viewModel.attachment.observe(this, Observer {
            handleAttachmentState(it)
        })
    }

    private fun handleAttachmentState(theAttachment: AttachmentEntity?) {
        theAttachment?.let {
            when (it.status) {
                Constants.AttachmentStatus.RECEIVED.status,
                Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status -> onModeReceived(it)
                Constants.AttachmentStatus.READED.status -> onModeReaded(it)
                Constants.AttachmentStatus.SENT.status -> onModeWhite()
                else -> hideStatus()
            }
        }
    }

    private fun hideStatus() {
        binding.apply {
            imageViewStatus.hide()
            frameStatus.hide()
        }
    }

    private fun onModeWhite() {
        binding.apply {
            imageViewStatus.show()
            frameStatus.show()
            imageViewStatus.setImageDrawable(root.context.getDrawable(R.drawable.ic_message_sent))
        }
    }

    private fun onModeReceived(attachmentEntity: AttachmentEntity) {
        binding.apply {
            imageViewStatus.show()
            frameStatus.show()
            imageViewStatus.setImageDrawable(root.context.getDrawable(R.drawable.ic_message_unread))
            if (file.messageAndAttachment?.isMine == 0) {
                hideViews(imageViewStatus, frameStatus)
            }
        }
        configTimer(attachmentEntity)
    }

    private fun onModeReaded(attachmentEntity: AttachmentEntity) {
        binding.apply {
            imageViewStatus.show()
            frameStatus.show()
            if (file.messageAndAttachment?.isMine == 1) { // is Mine
                imageViewStatus.setImageDrawable(root.context.getDrawable(R.drawable.ic_message_readed))
            } else {
                imageViewStatus.setImageDrawable(root.context.getDrawable(R.drawable.ic_baseline_check_circle))
            }
        }
        configTimer(attachmentEntity)
    }

    private fun configTimer(attachmentEntity: AttachmentEntity) {
        countDownTimer?.cancel()
        val endTime = attachmentEntity.totalSelfDestructionAt
        when {
            endTime > 0 -> showTimer(endTime, attachmentEntity)
            endTime == 0 -> showDestructionTime(attachmentEntity)
            else -> binding.textTimeAutodestruction.hide()
        }
    }

    private fun showTimer(endTime: Int, attachmentEntity: AttachmentEntity) {
        val currentTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
        val remainingTime = endTime - currentTime

        countDownTimer = object : CountDownTimer(
            TimeUnit.SECONDS.toMillis(endTime.toLong()) - System.currentTimeMillis(),
            1
        ) {
            override fun onFinish() {
                listener?.deleteAttachmentByDestructionTime(attachmentEntity.webId, position)
            }

            override fun onTick(millisUntilFinished: Long) {
                val text = Utils.getTimeWithDays(millisUntilFinished, showHours = true)
                binding.textTimeAutodestruction.text = text
            }
        }
        binding.textTimeAutodestruction.show()
        countDownTimer?.start()

    }

    private fun showDestructionTime(attachmentEntity: AttachmentEntity) {
        val timeToShow = Utils.convertItemOfTimeInSeconds(attachmentEntity.selfDestructionAt) * 1000
        val text = Utils.getTimeWithDays(timeToShow.toLong(), showHours = true)
        binding.textTimeAutodestruction.show()
        binding.textTimeAutodestruction.text = text
    }
}