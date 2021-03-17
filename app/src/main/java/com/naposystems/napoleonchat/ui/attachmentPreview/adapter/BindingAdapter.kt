package com.naposystems.napoleonchat.ui.attachmentPreview.adapter

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils

@BindingAdapter("attachmentImage")
fun bindGalleryImage(imageView: ImageView, attachmentEntity: AttachmentEntity?) {
    when (attachmentEntity?.type) {
        Constants.AttachmentType.IMAGE.type -> {
            val uri = Utils.getFileUri(
                imageView.context, attachmentEntity.fileName, Constants.CacheDirectories.IMAGES.folder
            )
            Glide.with(imageView)
                .load(uri)
                .centerInside()
                .into(imageView)
        }
        Constants.AttachmentType.GIF.type, Constants.AttachmentType.GIF_NN.type -> {
            Glide.with(imageView)
                .asGif()
                .load(attachmentEntity)
                .into(imageView)
        }
    }
}