package com.naposystems.pepito.utility.glideModelLoaders.galleryItem

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.naposystems.pepito.model.attachment.gallery.GalleryItem
import com.naposystems.pepito.utility.Constants
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream


class GalleryItemDataFetcher constructor(
    private val context: Context,
    private val galleryItem: GalleryItem
) :
    DataFetcher<InputStream> {

    override fun getDataClass() = InputStream::class.java

    override fun cleanup() {
        // Intentionally empty
    }

    override fun getDataSource() = DataSource.LOCAL

    override fun cancel() {
        // Intentionally empty.
    }

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        try {

            val fileInputStream: InputStream?
            val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val uri = if (galleryItem.attachmentType == Constants.AttachmentType.IMAGE.type)
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                else
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                val contentUri = ContentUris.withAppendedId(
                    uri,
                    galleryItem.id.toLong()
                )
                context.contentResolver.loadThumbnail(
                    contentUri,
                    Size((640), (480)),
                    null
                )
            } else {
                if (galleryItem.attachmentType == Constants.AttachmentType.IMAGE.type) {
                    MediaStore.Images.Thumbnails.getThumbnail(
                        context.contentResolver, galleryItem.id.toLong(),
                        MediaStore.Images.Thumbnails.MINI_KIND,
                        null as BitmapFactory.Options?
                    )
                } else {
                    MediaStore.Video.Thumbnails.getThumbnail(
                        context.contentResolver, galleryItem.id.toLong(),
                        MediaStore.Video.Thumbnails.MINI_KIND,
                        null as BitmapFactory.Options?
                    )
                }
            }
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, outputStream)
            val bitmapData: ByteArray = outputStream.toByteArray()
            fileInputStream = ByteArrayInputStream(bitmapData)
            callback.onDataReady(fileInputStream)
        } catch (e: Exception) {
            Timber.e(e)
            callback.onLoadFailed(e)
        }
    }
}