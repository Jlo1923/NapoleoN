package com.naposystems.pepito.ui.attachmentPreview.adapter

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.utility.Constants

@BindingAdapter("attachmentImage")
fun bindGalleryImage(imageView: ImageView, attachment: Attachment) {

    when (attachment.type) {
        Constants.AttachmentType.IMAGE.type -> {
            Glide.with(imageView)
                .load(attachment)
                .centerInside()
                .into(imageView)
        }
        Constants.AttachmentType.GIF.type, Constants.AttachmentType.GIF_NN.type -> {
            Glide.with(imageView)
                .asGif()
                .load(attachment)
                .centerInside()
                .into(imageView)
        }
    }
}