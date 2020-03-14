package com.naposystems.pepito.utility.glideModelLoaders.attachment

import android.R.attr.path
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.util.Size
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.FileManager
import com.naposystems.pepito.utility.Utils
import java.io.*


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
            val subFolder =
                FileManager.getSubfolderByAttachmentType(attachmentType = attachment.type)

            val fileUri = Utils.getFileUri(
                context = context,
                fileName = attachment.uri,
                subFolder = subFolder
            )
            if (attachment.type == Constants.AttachmentType.IMAGE.type) {

                val inputStream = context.contentResolver.openInputStream(fileUri)

                callback.onDataReady(inputStream)
            }

            if (attachment.type == Constants.AttachmentType.VIDEO.type) {
                callback.onDataReady(FileManager.getThumbnailFromVideo(fileUri.toString()))
            }
        } catch (e: Exception) {
            callback.onLoadFailed(e)
        }
    }
}