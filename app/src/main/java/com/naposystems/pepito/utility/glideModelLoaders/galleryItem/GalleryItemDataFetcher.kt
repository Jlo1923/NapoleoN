package com.naposystems.pepito.utility.glideModelLoaders.galleryItem

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.naposystems.pepito.model.attachment.gallery.GalleryItem
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

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

            val fileDescriptor = context.contentResolver
                .openAssetFileDescriptor(galleryItem.contentUri!!, "r")
            val fileInputStream = FileInputStream(fileDescriptor!!.fileDescriptor)

            callback.onDataReady(fileInputStream)
        } catch (e: Exception) {
            Timber.e(e)
            callback.onLoadFailed(e)
        }
    }
}