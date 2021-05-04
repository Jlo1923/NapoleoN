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
import com.naposystems.napoleonchat.utility.extensions.show
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MultipleAttachmentPreviewImageFragment(
    val file: MultipleAttachmentFileItem,
    val position: Int
) : BaseFragment() {

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

    private fun handleAttachmentState(theAttachment: AttachmentEntity?) {
        theAttachment?.let {
            when (it.status) {
                Constants.AttachmentStatus.RECEIVED.status,
                Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status -> onModeReceived()
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

    private fun onModeReceived() {
        binding.apply {
            imageViewStatus.show()
            frameStatus.show()
            imageViewStatus.setImageDrawable(root.context.getDrawable(R.drawable.ic_message_unread))
        }
    }

    private fun onModeReaded(attachmentEntity: AttachmentEntity) {
        binding.apply {
            imageViewStatus.show()
            frameStatus.show()
            imageViewStatus.setImageDrawable(root.context.getDrawable(R.drawable.ic_message_readed))
        }
        configTimer(attachmentEntity)
    }

    private fun configTimer(attachmentEntity: AttachmentEntity) {

        countDownTimer?.cancel()
        val endTime = attachmentEntity.totalSelfDestructionAt
        if (endTime > 0) {
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
        } else {
            binding.textTimeAutodestruction.hide()
        }

    }

    private fun loadImageFromBody() {
        try {
            binding.apply {
                Glide.with(root.context)
                    .load(file.messageAndAttachment?.attachment?.body)
                    .into(imagePreview)
            }
        } catch (exception: Exception) {

        }
    }

    private fun loadImage() {
        try {
            binding.apply {
                Glide.with(root.context).load(file.contentUri)
                    .into(imagePreview)
            }
        } catch (exception: Exception) {

        }
    }

    fun setListener(listener: MultipleAttachmentPreviewListener) {
        this.listener = listener
    }

    override fun onPause() {
        super.onPause()
        binding.apply { imagePreview.fitToScreen() }
    }

}