package com.naposystems.pepito.ui.attachmentGallery.adapter

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.naposystems.pepito.model.attachment.gallery.GalleryItem
import com.naposystems.pepito.utility.Constants

@BindingAdapter("galleryItemThumbnail")
fun binGalleryItemThumbnail(imageView: ImageView, galleryItem: GalleryItem) {

    Glide.with(imageView)
        .load(galleryItem)
        .into(imageView)
}

@BindingAdapter("mediaTypeIcon")
fun bindMediaTypeIcon(imageView: ImageView, galleryItem: GalleryItem) {
    if (galleryItem.attachmentType == Constants.AttachmentType.VIDEO.type) {
        imageView.visibility = View.VISIBLE
    }
}