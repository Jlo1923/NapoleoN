package com.naposystems.napoleonchat.ui.multipreview.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ViewMultipleAttachmentTabBinding
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentItemMessage
import com.naposystems.napoleonchat.ui.multipreview.viewmodels.MultipleAttachmentPreviewItemViewModel
import com.naposystems.napoleonchat.utility.extensions.getBlurTransformation
import com.naposystems.napoleonchat.utility.extensions.isVideo
import com.naposystems.napoleonchat.utility.extensions.show

@SuppressLint("ViewConstructor")
class ViewMultipleAttachmentTabView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val binding: ViewMultipleAttachmentTabBinding by lazy {
        ViewMultipleAttachmentTabBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun bindFile(file: MultipleAttachmentFileItem) {
        loadImage(file)
        showViewIfIsVideo(file)
    }

    fun selected(isSelected: Boolean) {
        val resources = binding.root.context.resources
        val selectStroke = resources.getDimension(R.dimen.multiple_attachment_tab_select_stroke)
        val dimen = R.dimen.multiple_attachment_tab_un_select_stroke
        val unSelectStroke = resources.getDimension(dimen)
        val strokeWidth = if (isSelected) selectStroke else unSelectStroke
        binding.cardView.strokeWidth = strokeWidth.toInt()
    }

    private fun showViewIfIsVideo(file: MultipleAttachmentFileItem) =
        binding.layoutVideo.show(file.isVideo())

    private fun loadImage(file: MultipleAttachmentFileItem) =
        if (file.messageAndAttachment == null) {
            loadImageFromUri(file)
        } else {
            loadImageFromThumbnailUri(file.messageAndAttachment)
        }

    private fun loadImageFromThumbnailUri(messageAndAttachment: MultipleAttachmentItemMessage) =
        binding.apply {
            Glide.with(root.context)
                .load(messageAndAttachment.attachment.thumbnailUri)
                .transform(*getBlurTransformation(root.context))
                .into(imageFolderThumbnail)
        }

    private fun loadImageFromUri(file: MultipleAttachmentFileItem) = binding.apply {
        Glide.with(root.context)
            .load(file.contentUri)
            .into(imageFolderThumbnail)
    }

}