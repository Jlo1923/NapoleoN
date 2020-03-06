package com.naposystems.pepito.ui.attachmentPreview.adapter

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.naposystems.pepito.model.attachment.gallery.GalleryItem
import com.naposystems.pepito.utility.GlideManager
import java.io.File

@BindingAdapter("galleryImage")
fun bindGalleryImage(imageView: ImageView, galleryItem: GalleryItem) {

    val context = imageView.context

    when (galleryItem.mediaType) {
        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> {
            var bitmap: Bitmap
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
            )
        }
        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> {
            GlideManager.loadFile(imageView, File(galleryItem.thumbnailUri!!.path!!))
        }
    }
}