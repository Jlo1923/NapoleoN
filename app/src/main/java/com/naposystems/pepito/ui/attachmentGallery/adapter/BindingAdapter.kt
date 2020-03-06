package com.naposystems.pepito.ui.attachmentGallery.adapter

import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.naposystems.pepito.model.attachment.gallery.GalleryItem

@BindingAdapter("galleryItemThumbnail")
fun binGalleryItemThumbnail(imageView: ImageView, galleryItem: GalleryItem) {

    Glide.with(imageView)
        .load(galleryItem)
        .into(imageView)
}

@BindingAdapter("mediaTypeIcon")
fun bindMediaTypeIcon(imageView: ImageView, galleryItem: GalleryItem) {
    if (galleryItem.mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
        imageView.visibility = View.VISIBLE
    }
}