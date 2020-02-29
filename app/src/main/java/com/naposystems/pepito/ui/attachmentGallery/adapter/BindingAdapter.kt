package com.naposystems.pepito.ui.attachmentGallery.adapter

import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.naposystems.pepito.model.attachment.gallery.GalleryItem
import com.naposystems.pepito.utility.GlideManager
import java.io.File

@BindingAdapter("galleryItemThumbnail")
fun binGalleryItemThumbnail(imageView: ImageView, galleryItem: GalleryItem) {

    if (galleryItem.thumbnailUri != null) {
        GlideManager.loadFile(imageView, File(galleryItem.thumbnailUri!!.path!!))
    } else if (galleryItem.contentUri != null) {

        /*var bitmap: Bitmap
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ImageDecoder.createSource(context.contentResolver, galleryItem.contentUri!!)
                .also { source ->
                    ImageDecoder.decodeBitmap(source).also { bitmapDecoded ->
                        bitmap = bitmapDecoded
                    }
                }
        } else {
            bitmap =
                MediaStore.Images.Media.getBitmap(
                    context.contentResolver,
                    galleryItem.contentUri
                )
        }

        GlideManager.loadBitmap(
            imageView,
            bitmap
        )*/
    }
}

@BindingAdapter("mediaTypeIcon")
fun bindMediaTypeIcon(imageView: ImageView, galleryItem: GalleryItem) {
    val context = imageView.context

    if (galleryItem.mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
        imageView.visibility = View.VISIBLE
    }
}