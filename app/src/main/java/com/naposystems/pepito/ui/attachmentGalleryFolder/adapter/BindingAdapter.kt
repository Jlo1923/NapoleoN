package com.naposystems.pepito.ui.attachmentGalleryFolder.adapter

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.naposystems.pepito.model.attachment.gallery.GalleryFolder
import com.naposystems.pepito.utility.GlideManager
import timber.log.Timber
import java.io.File

@BindingAdapter("folderThumbnail")
fun binFolderThumbnail(imageView: ImageView, galleryFolder: GalleryFolder) {

    try {
        val context = imageView.context

        when {
            galleryFolder.bitmapThumbnail != null -> {
                GlideManager.loadBitmap(
                    imageView,
                    galleryFolder.bitmapThumbnail
                )
            }
            galleryFolder.thumbnailUri != null -> {
                GlideManager.loadFile(imageView, File(galleryFolder.thumbnailUri!!.path!!))
            }
            galleryFolder.contentUri != null -> {

                var bitmap: Bitmap
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.createSource(context.contentResolver, galleryFolder.contentUri!!)
                        .also { source ->
                            ImageDecoder.decodeBitmap(source).also { bitmapDecoded ->
                                bitmap = bitmapDecoded
                            }
                        }
                } else {
                    bitmap =
                        MediaStore.Images.Media.getBitmap(
                            context.contentResolver,
                            galleryFolder.contentUri
                        )
                }

                GlideManager.loadBitmap(
                    imageView,
                    bitmap
                )
            }
        }
    } catch (e: Exception) {
        Timber.e(e)
    }
}