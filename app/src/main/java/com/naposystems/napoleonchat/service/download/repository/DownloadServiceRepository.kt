package com.naposystems.napoleonchat.service.download.repository

import android.content.Context
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.download.contract.IContractDownloadService
import com.naposystems.napoleonchat.source.local.datasource.attachment.AttachmentLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.message.MessageLocalDataSource
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.utility.*
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.utility.Constants.AttachmentStatus.DOWNLOAD_CANCEL
import com.naposystems.napoleonchat.utility.Constants.AttachmentStatus.DOWNLOAD_COMPLETE
import com.naposystems.napoleonchat.utility.Constants.AttachmentType.*
import com.naposystems.napoleonchat.utility.Constants.CacheDirectories.*
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import timber.log.Timber
import java.io.*
import java.util.concurrent.CancellationException
import javax.inject.Inject

class DownloadServiceRepository @Inject constructor(
    private val context: Context,
    private val napoleonApi: NapoleonApi,
    private val attachmentLocalDataSource: AttachmentLocalDataSource,
    private val messageLocalDataSource: MessageLocalDataSource
) : IContractDownloadService.Repository {

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.IO)
    private var uploadJob: Job? = null

    override fun downloadAttachment(attachmentEntity: AttachmentEntity) {

        uploadJob = coroutineScope.launch {
            val fileName = "${System.currentTimeMillis()}.${attachmentEntity.extension}"
            try {
                updateAttachmentStatus(
                    attachmentEntity,
                    Constants.AttachmentStatus.DOWNLOADING.status
                )
                val response = napoleonApi.downloadFileByUrl(attachmentEntity.body)
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        saveFileLocally(attachmentEntity, fileName, body)
                    }
                } else {
                    updateAttachmentStatus(attachmentEntity, DOWNLOAD_CANCEL.status)
                    publishEventError(attachmentEntity)
                }
            } catch (e: Exception) {
                updateAttachmentStatus(attachmentEntity, DOWNLOAD_CANCEL.status)
                publishEventError(attachmentEntity)
            }
        }
    }

    override fun updateAttachment(attachmentEntity: AttachmentEntity) =
        attachmentLocalDataSource.updateAttachment(attachmentEntity)

    override fun cancelDownload() {
        uploadJob?.let { if (it.isActive) it.cancel() }
    }

    private fun saveFileLocally(
        attachment: AttachmentEntity,
        fileName: String,
        body: ResponseBody
    ) {
        val folder = getFolderByAttachmentType(attachment)
        val path = File(context.cacheDir!!, folder)
        if (!path.exists()) path.mkdirs()

        val file = File(path, fileName)
        attachment.fileName = fileName
        updateAttachment(attachment)

        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {

            Timber.d("File Size=${body.contentLength()}")
            val contentLength = body.contentLength()
            inputStream = body.byteStream()
            outputStream = file.outputStream()
            val data = ByteArray(4096)
            var count: Int
            var progress = 0
            while (inputStream.read(data).also { count = it } != -1) {
                //yield()
                outputStream.write(data, 0, count)
                progress += count
                val finalPercentage = (progress * 100 / contentLength)
                if (finalPercentage > 0) {
                    publishEventProgress(attachment, finalPercentage)
                }
            }
            outputStream.flush()

            attachment.thumbnailUri = file.toURI().toString()

            if (BuildConfig.ENCRYPT_API && attachment.type != GIF_NN.type) {
                saveEncryptedFile(attachment)
            } else {
                updateAttachmentStatus(attachment, DOWNLOAD_COMPLETE.status)
                publishEventTryNext()
            }

        } catch (e: CancellationException) {
            updateAttachmentStatus(attachment, DOWNLOAD_CANCEL.status)
            publishEventError(attachment)
        } catch (e: IOException) {
            updateAttachmentStatus(attachment, DOWNLOAD_CANCEL.status)
            publishEventError(attachment)
            Timber.d("Failed to save the file!")
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }

    private fun updateAttachmentStatus(
        attachment: AttachmentEntity, status: Int
    ) = attachment.apply {
        this.status = status
        updateAttachment(attachment)
    }

    private fun saveEncryptedFile(attachmentEntity: AttachmentEntity) {
        FileManager.copyEncryptedFile(context, attachmentEntity)
        updateAttachmentStatus(attachmentEntity, DOWNLOAD_COMPLETE.status)
        publishEventTryNext()
    }

    private fun getFolderByAttachmentType(
        attachment: AttachmentEntity
    ): String = when (attachment.type) {
        IMAGE.type, LOCATION.type -> IMAGES.folder
        AUDIO.type -> AUDIOS.folder
        VIDEO.type -> VIDEOS.folder
        DOCUMENT.type -> DOCUMENTOS.folder
        GIF.type -> GIFS.folder
        GIF_NN.type -> GIFS.folder
        else -> IMAGES.folder
    }

    private fun publishEventProgress(
        attachment: AttachmentEntity,
        finalPercentage: Long
    ) = RxBus.publish(RxEvent.MultiDownloadProgress(attachment, finalPercentage.toFloat()))

    private fun publishEventTryNext() = RxBus.publish(RxEvent.MultiDownloadTryNextAttachment())

    private fun publishEventError(attachment: AttachmentEntity) {
        /**
         * Si ha fallado, debemos tomar el attachment para consultar su mensaje padre
         * Este mensaje padre debera ser marcado como error, para indiciar en la conversation que
         * la descarga de uno o varios archivos de ese mensaje ha marcado ERROR
         * Y la persona debera iniciar la descarga manualmente
         */
        GlobalScope.launch {
            val message = messageLocalDataSource.getMessageById(attachment.messageId, false)
            val theMsgParent = message?.messageEntity
            theMsgParent?.let {
                it.status = Constants.MessageStatus.ERROR.status
                messageLocalDataSource.updateMessage(it)
            }
            RxBus.publish(RxEvent.MultiDownloadError(attachment, "File not downloaded", null))
        }
    }

}