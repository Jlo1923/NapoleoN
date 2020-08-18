package com.naposystems.napoleonchat.ui.previewMedia.adapter

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.naposystems.napoleonchat.entity.message.MessageAndAttachment
import com.naposystems.napoleonchat.utility.Constants
import timber.log.Timber

@BindingAdapter("previewImage")
fun bindPreviewImage(imageView: ImageView, messageAndAttachment: MessageAndAttachment) {
    try {
        if (messageAndAttachment.attachmentList.isNotEmpty()) {
            imageView.visibility = View.VISIBLE
            val firstAttachment = messageAndAttachment.attachmentList[0]

            when (firstAttachment.type) {
                Constants.AttachmentType.IMAGE.type, Constants.AttachmentType.LOCATION.type -> {
                    Glide.with(imageView)
                        .load(firstAttachment)
                        .into(imageView)
                }
                Constants.AttachmentType.GIF.type -> {
                    Glide.with(imageView)
                        .asGif()
                        .load(firstAttachment)
                        .into(imageView)
                }
            }
        }
    } catch (e: Exception) {
        Timber.e(e)
    }
}