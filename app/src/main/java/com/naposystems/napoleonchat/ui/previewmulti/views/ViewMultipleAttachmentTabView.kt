package com.naposystems.napoleonchat.ui.previewmulti.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ViewMultipleAttachmentTabBinding
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem

class ViewMultipleAttachmentTabView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val binding: ViewMultipleAttachmentTabBinding by lazy {
        ViewMultipleAttachmentTabBinding.inflate(
            LayoutInflater.from(context), this, true
        )
    }

    private fun loadImage(file: MultipleAttachmentFileItem) {
        try {
            binding.apply {
                Glide.with(root.context).load(file.contentUri)
                    .into(imageFolderThumbnail)
            }
        } catch (exception: Exception) {

        }
    }

    fun bindFile(file: MultipleAttachmentFileItem) {
        loadImage(file)
    }

    fun selected(isSelected: Boolean) {
        val resources = binding.root.context.resources
        val selectStroke = resources.getDimension(R.dimen.multiple_attachment_tab_select_stroke)
        val unSelectStroke =
            resources.getDimension(R.dimen.multiple_attachment_tab_un_select_stroke)
        val strokeWidth = if (isSelected) selectStroke else unSelectStroke
        binding.cardView.strokeWidth = strokeWidth.toInt()
    }

}