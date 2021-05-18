package com.naposystems.napoleonchat.ui.multipreview.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ViewMultipleAttachmentTabBinding
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.multipreview.viewmodels.MultipleAttachmentPreviewItemViewModel
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.extensions.hide
import com.naposystems.napoleonchat.utility.extensions.isVideo
import com.naposystems.napoleonchat.utility.extensions.show

class ViewMultipleAttachmentTabView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    val viewModel: MultipleAttachmentPreviewItemViewModel
) : FrameLayout(context, attrs, defStyle), LifecycleOwner {

    private val registry: LifecycleRegistry = LifecycleRegistry(this)

    private val binding: ViewMultipleAttachmentTabBinding by lazy {
        ViewMultipleAttachmentTabBinding.inflate(
            LayoutInflater.from(context), this, true
        )
    }

    private fun loadImage(file: MultipleAttachmentFileItem) {

        if (file.messageAndAttachment == null) {
            try {
                binding.apply {
                    Glide.with(root.context).load(file.contentUri)
                        .into(imageFolderThumbnail)
                }
            } catch (exception: Exception) {

            }
        } else {
            try {
                binding.apply {
                    Glide.with(root.context)
                        .load(file.messageAndAttachment.attachment.body)
                        .into(imageFolderThumbnail)
                }
            } catch (exception: Exception) {

            }
        }
    }

    fun bindFile(file: MultipleAttachmentFileItem) {
        loadImage(file)
        checksVideo(file)
        file.messageAndAttachment?.let {
            viewModel.setAttachmentAndLaunchLiveData(it.attachment.webId)
            bindViewModel()
        }
    }

    private fun bindViewModel() {
        viewModel.attachment.observe(this, Observer {
            handleAttachment(it)
        })
    }

    private fun handleAttachment(theAttachment: AttachmentEntity?) {
        theAttachment?.let {
            when (it.status) {
                Constants.AttachmentStatus.RECEIVED.status,
                Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status -> onModeReceived()
                Constants.AttachmentStatus.READED.status -> onModeReaded()
                else -> hideStatus()
            }
        }
    }

    private fun hideStatus() {
        binding.apply {
            imageViewStatus.hide()
        }
    }

    private fun onModeReceived() {
        binding.apply {
            imageViewStatus.show()
            imageViewStatus.setImageDrawable(root.context.getDrawable(R.drawable.ic_message_unread))
        }
    }

    private fun onModeReaded() {
        binding.apply {
            imageViewStatus.show()
            imageViewStatus.setImageDrawable(root.context.getDrawable(R.drawable.ic_message_readed))
        }
    }

    fun selected(isSelected: Boolean) {
        val resources = binding.root.context.resources
        val selectStroke = resources.getDimension(R.dimen.multiple_attachment_tab_select_stroke)
        val unSelectStroke =
            resources.getDimension(R.dimen.multiple_attachment_tab_un_select_stroke)
        val strokeWidth = if (isSelected) selectStroke else unSelectStroke
        binding.cardView.strokeWidth = strokeWidth.toInt()
    }

    private fun checksVideo(file: MultipleAttachmentFileItem) =
        binding.layoutVideo.show(file.isVideo())

    override fun getLifecycle(): Lifecycle {
        return registry
    }

}