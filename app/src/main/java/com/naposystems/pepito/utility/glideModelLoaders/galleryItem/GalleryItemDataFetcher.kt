package com.naposystems.pepito.utility.glideModelLoaders.galleryItem

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.naposystems.pepito.model.attachment.gallery.GalleryItem
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.FileManager
import timber.log.Timber
import java.io.*

class GalleryItemDataFetcher constructor(
    private val context: Context,
    private val galleryItem: GalleryItem
) :
    DataFetcher<InputStream> {

    override fun getDataClass() = InputStream::class.java

    override fun cleanup() {
        //TODO: Limpiar el InputStream close(), and clean()
    }

    override fun getDataSource() = DataSource.LOCAL

    override fun cancel() {
        // Intentionally empty.
    }

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        try {

            var fileInputStream: InputStream? = null
            if (galleryItem.attachmentType == Constants.AttachmentType.IMAGE.type) {
                val fileDescriptor = context.contentResolver
                    .openAssetFileDescriptor(galleryItem.contentUri!!, "r")
                fileInputStream = FileInputStream(fileDescriptor!!.fileDescriptor)
            }

            if (galleryItem.attachmentType == Constants.AttachmentType.VIDEO.type) {

                val videoId = ContentUris.parseId(galleryItem.contentUri!!)

                val bitmap = MediaStore.Video.Thumbnails.getThumbnail(
                    context.contentResolver,
                    videoId,
                    MediaStore.Images.Thumbnails.MINI_KIND,
                    null
                )

                val outputStream = ByteArrayOutputStream()
                bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                val bitmapData: ByteArray = outputStream.toByteArray()
                fileInputStream = ByteArrayInputStream(bitmapData)
            }

            callback.onDataReady(fileInputStream)
        } catch (e: Exception) {
            Timber.e(e)
            callback.onLoadFailed(e)
        }
    }
}