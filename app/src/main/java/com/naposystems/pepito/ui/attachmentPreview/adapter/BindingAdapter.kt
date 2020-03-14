package com.naposystems.pepito.ui.attachmentPreview.adapter

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.GlideManager
import java.io.File

@BindingAdapter("attachmentImage")
fun bindGalleryImage(imageView: ImageView, attachment: Attachment) {

    when (attachment.type) {
        Constants.AttachmentType.IMAGE.type -> {
            Glide.with(imageView)
                .load(attachment)
                .into(imageView)
        }
    }
}