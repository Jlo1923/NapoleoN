package com.naposystems.pepito.utility.glideModelLoaders.attachment

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Utils
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class AttachmentDataFetcher constructor(
    private val context: Context,
    private val attachment: Attachment
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
            when (attachment.origin) {
                Constants.AttachmentOrigin.DOWNLOADED.origin -> {
                    val encryptedFile = Utils.getEncryptedFile(context, File(attachment.uri))

                    callback.onDataReady(encryptedFile.openFileInput())
                }
                Constants.AttachmentOrigin.CAMERA.origin -> {
                    val file = File(attachment.uri)
                    callback.onDataReady(file.inputStream())
                }
                Constants.AttachmentOrigin.GALLERY.origin -> {
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        ContentUris.parseId(Uri.parse(attachment.uri))
                    )

                    val parcelFileDescriptor =
                        context.contentResolver.openFileDescriptor(contentUri, "r")

                    val fileInputStream = FileInputStream(parcelFileDescriptor!!.fileDescriptor)

                    callback.onDataReady(fileInputStream)
                }
            }
        } catch (e: Exception) {
            callback.onLoadFailed(e)
        }
    }
}