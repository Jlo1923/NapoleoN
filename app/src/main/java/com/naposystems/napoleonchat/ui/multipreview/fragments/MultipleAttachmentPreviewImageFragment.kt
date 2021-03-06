package com.naposystems.napoleonchat.ui.multipreview.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.FragmentMultipleAttachmentPreviewImageBinding
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.ui.baseFragment.BaseFragment
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.multipreview.listeners.MultipleAttachmentPreviewListener
import com.naposystems.napoleonchat.ui.multipreview.viewmodels.MultipleAttachmentPreviewItemViewModel
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.extensions.hide
import com.naposystems.napoleonchat.utility.extensions.hideViews
import com.naposystems.napoleonchat.utility.extensions.show
import com.naposystems.napoleonchat.utility.extensions.showViews
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import com.naposystems.napoleonchat.utility.zoom.ZoomImageListener
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MultipleAttachmentPreviewImageFragment(
    val file: MultipleAttachmentFileItem,
    val position: Int
) : BaseFragment(), ZoomImageListener {

    @Inject
    override lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: MultipleAttachmentPreviewItemViewModel by viewModels {
        viewModelFactory
    }

    private lateinit var binding: FragmentMultipleAttachmentPreviewImageBinding
    private var listener: MultipleAttachmentPreviewListener? = null

    private var countDownTimer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMultipleAttachmentPreviewImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.imagePreview.setOnClickListener { listener?.changeVisibilityOptions() }
        if (file.messageAndAttachment == null) {
            loadImage()
        } else {
            loadImageFromBody()
        }
        binding.imagePreview.setListener(this)
    }

    override fun onResume() {
        super.onResume()
        file.messageAndAttachment?.let {
            viewModel.setAttachmentAndLaunchLiveData(it.attachment.webId)
            bindViewModel()
        }
    }

    private fun bindViewModel() {
        viewModel.attachment.observe(this, Observer {
            handleAttachmentState(it)
        })
    }

    override fun onZoomMode() {
        listener?.blockPager()
    }

    override fun onNormalMode() {
        listener?.unBlockPager()
    }

    private fun handleAttachmentState(theAttachment: AttachmentEntity?) {
        theAttachment?.let {
            configTimer(it)
            when (it.status) {
                Constants.AttachmentStatus.RECEIVED.status,
                Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status -> onModeReceived(it)
                Constants.AttachmentStatus.READED.status -> onModeRead(it)
                Constants.AttachmentStatus.SENT.status -> onModeWhite()
                Constants.AttachmentStatus.UPLOAD_CANCEL.status -> onModeError(it)
                else -> hideStatus()
            }
        }
    }

    private fun hideStatus() = binding.apply {
        imageViewStatus.hide()
        frameStatus.hide()
    }

    private fun onModeWhite() = binding.apply {
        imageViewStatus.show()
        frameStatus.show()
        imageViewStatus.setImageDrawable(root.context.getDrawable(R.drawable.ic_message_sent))
    }

    private fun onModeError(attachmentEntity: AttachmentEntity) = binding.apply {
        imageViewStatus.show()
        frameStatus.show()
        imageViewStatus.setImageDrawable(root.context.getDrawable(R.drawable.ic_message_error))
        configTimer(attachmentEntity)
    }

    private fun onModeReceived(attachmentEntity: AttachmentEntity) = binding.apply {
        imageViewStatus.show()
        frameStatus.show()
        imageViewStatus.setImageDrawable(root.context.getDrawable(R.drawable.ic_message_unread))
        if (file.messageAndAttachment?.isMine == 0) {
            hideViews(imageViewStatus, frameStatus)
        }
    }

    private fun onModeRead(attachmentEntity: AttachmentEntity) = binding.apply {
        showViews(imageViewStatus, frameStatus)
        if (file.messageAndAttachment?.isMine == 1) { // is Mine
            imageViewStatus.setImageDrawable(root.context.getDrawable(R.drawable.ic_message_readed))
        } else {
            imageViewStatus.setImageDrawable(root.context.getDrawable(R.drawable.ic_baseline_check_circle))
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

    private fun showDestructionTime(attachmentEntity: AttachmentEntity) {
        val timeToShow = Utils.convertItemOfTimeInSeconds(attachmentEntity.selfDestructionAt) * 1000
        val text = Utils.getTimeWithDays(timeToShow.toLong(), showHours = true)
        binding.textTimeAutodestruction.show()
        binding.textTimeAutodestruction.text = text
    }

    private fun showTimer(endTime: Int, attachmentEntity: AttachmentEntity) {

        val remainingTime = TimeUnit.SECONDS.toMillis(endTime.toLong()) - System.currentTimeMillis()

        countDownTimer = object : CountDownTimer(remainingTime, 1) {

            override fun onTick(millisUntilFinished: Long) {
                val text = Utils.getTimeWithDays(millisUntilFinished, showHours = true)
                binding.textTimeAutodestruction.text = text
            }

            override fun onFinish() {
                listener?.deleteAttachmentByDestructionTime(attachmentEntity.webId, position)
            }

        }

        countDownTimer?.start()
        binding.textTimeAutodestruction.show()

    }

    private fun loadImageFromBody() = binding.apply {
        try {
            Glide.with(root.context)
                .load(file.messageAndAttachment?.attachment?.thumbnailUri?.toString())
                .into(imagePreview)
        } catch (e: Exception) {
        }
    }

    private fun loadImage() = binding.apply {
        try {
            Glide.with(root.context)
                .load(file.contentUri)
                .into(imagePreview)
        } catch (e: Exception) {
        }
    }

    fun setListener(listener: MultipleAttachmentPreviewListener) {
        this.listener = listener
    }

    override fun onPause() {
        super.onPause()
        binding.apply { imagePreview.fitToScreen() }
        countDownTimer?.cancel()
    }

}