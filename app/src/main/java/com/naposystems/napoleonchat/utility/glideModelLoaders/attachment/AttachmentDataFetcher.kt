package com.naposystems.napoleonchat.utility.glideModelLoaders.attachment

import android.content.Context
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.FileManager
import com.naposystems.napoleonchat.utility.Utils
import timber.log.Timber
import java.io.InputStream

class AttachmentDataFetcher constructor(
    private val context: Context,
    private val attachmentEntity: AttachmentEntity
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
            val subFolder =
                FileManager.getSubfolderByAttachmentType(attachmentType = attachmentEntity.type)

            val fileUri = Utils.getFileUri(
                context = context,
                fileName = attachmentEntity.fileName,
                subFolder = subFolder
            )

            when (attachmentEntity.type) {
                Constants.AttachmentType.IMAGE.type,
                Constants.AttachmentType.GIF.type,
                Constants.AttachmentType.GIF_NN.type,
                Constants.AttachmentType.LOCATION.type -> {

                    if (BuildConfig.ENCRYPT_API && attachmentEntity.type != Constants.AttachmentType.GIF_NN.type) {
                        val extension = attachmentEntity.extension
                        if (attachmentEntity.webId.isNotEmpty()) {
                            val fileName = "${attachmentEntity.webId}.$extension"
                            callback.onDataReady(
                                FileManager.getFileInputStreamFromEncryptedFile(
                                    context,
                                    fileName,
                                    subFolder
                                )
                            )
                        } else {
                            val inputStream = context.contentResolver.openInputStream(fileUri)
                            callback.onDataReady(inputStream)
                        }
                    } else {
                        val inputStream = context.contentResolver.openInputStream(fileUri)
                        callback.onDataReady(inputStream)
                    }
                }
                Constants.AttachmentType.VIDEO.type -> {
                    Timber.d("loadData VIDEO")
                    callback.onDataReady(
                        FileManager.getThumbnailFromVideo(
                            fileUri.toString()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            callback.onLoadFailed(e)
        }
    }
}