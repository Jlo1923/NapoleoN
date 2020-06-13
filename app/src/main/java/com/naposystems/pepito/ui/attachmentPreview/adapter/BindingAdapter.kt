package com.naposystems.pepito.ui.attachmentPreview.adapter

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Utils

@BindingAdapter("attachmentImage")
fun bindGalleryImage(imageView: ImageView, attachment: Attachment?) {
    when (attachment?.type) {
        Constants.AttachmentType.IMAGE.type -> {
            val uri = Utils.getFileUri(
                imageView.context, attachment.uri, Constants.NapoleonCacheDirectories.IMAGES.folder
            )
            Glide.with(imageView)
                .load(uri)
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