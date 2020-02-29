package com.naposystems.pepito.ui.attachmentGalleryFolder.adapter

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.naposystems.pepito.model.attachment.gallery.GalleryFolder
import com.naposystems.pepito.utility.GlideManager
import java.io.File

@BindingAdapter("folderThumbnail")
fun binFolderThumbnail(imageView: ImageView, galleryFolder: GalleryFolder) {

    val context = imageView.context

    if (galleryFolder.thumbnailUri != null) {
        GlideManager.loadFile(imageView, File(galleryFolder.thumbnailUri!!.path!!))
    } else if (galleryFolder.contentUri != null) {

        var bitmap: Bitmap
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ImageDecoder.createSource(context.contentResolver, galleryFolder.contentUri!!)
                .also { source ->
                    ImageDecoder.decodeBitmap(source).also { bitmapDecoded ->
                        bitmap = bitmapDecoded
                    }
                }
        } else {
            bitmap =
                MediaStore.Images.Media.getBitmap(context.contentResolver, galleryFolder.contentUri)
        }

        GlideManager.loadBitmap(
            imageView,
            bitmap
        )
    }
}