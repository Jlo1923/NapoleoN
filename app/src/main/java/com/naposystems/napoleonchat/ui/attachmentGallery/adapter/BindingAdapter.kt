package com.naposystems.napoleonchat.ui.attachmentGallery.adapter

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.naposystems.napoleonchat.model.attachment.gallery.GalleryItem
import com.naposystems.napoleonchat.utility.Constants

@BindingAdapter("galleryItemThumbnail")
fun binGalleryItemThumbnail(imageView: ImageView, galleryItem: GalleryItem) {

    Glide.with(imageView)
        .load(galleryItem)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(imageView)
}

@BindingAdapter("mediaTypeIcon")
fun bindMediaTypeIcon(imageView: ImageView, galleryItem: GalleryItem) {
    if (galleryItem.attachmentType == Constants.AttachmentType.VIDEO.type) {
        imageView.visibility = View.VISIBLE
    }
}