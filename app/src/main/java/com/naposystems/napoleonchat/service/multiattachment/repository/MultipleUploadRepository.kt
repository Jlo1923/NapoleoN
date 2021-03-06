package com.naposystems.napoleonchat.service.multiattachment.repository

import android.content.Context
import android.webkit.MimeTypeMap
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.multiattachment.contract.IContractMultipleUpload
import com.naposystems.napoleonchat.source.local.datasource.attachment.AttachmentLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.message.MessageLocalDataSource
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.conversation.attachment.AttachmentResDTO
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Constants.AttachmentType
import com.naposystems.napoleonchat.utility.Constants.MessageStatus
import com.naposystems.napoleonchat.utility.Constants.MessageStatus.SENDING
import com.naposystems.napoleonchat.utility.FileManager
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.webService.ProgressRequestBody
import com.vincent.videocompressor.VideoCompressK
import com.vincent.videocompressor.VideoCompressResult
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject

class MultipleUploadRepository @Inject constructor(
    private val context: Context,
    private val msgDataSource: MessageLocalDataSource,
    private val attachmentDataSource: AttachmentLocalDataSource,
    private val preferences: SharedPreferencesManager,
    private val napoleonApi: NapoleonApi
) : IContractMultipleUpload.Repository {

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.IO)
    private var uploadJob: Job? = null

    lateinit var currentAttachment: AttachmentEntity
    lateinit var currentMessage: MessageEntity

    override fun uploadAttachment(
        attachmentEntity: AttachmentEntity,
        messageEntity: MessageEntity
    ) {

        currentAttachment = attachmentEntity
        currentMessage = messageEntity

        uploadJob = coroutineScope.launch {

            try {
                updateMessageStatus(messageEntity, SENDING.status)

                currentAttachment.status = Constants.AttachmentStatus.SENDING.status
                updateAttachment(currentAttachment)

                publishEventStart()

                val pairFiles = getDestFileForCompress(attachmentEntity)
                compressVideo(attachmentEntity, pairFiles.first, pairFiles.second, this)
                    .collect { handleVideoCompressResult(it, this as Job) }
            } catch (exception: Exception) {
                publishEventError()
            }
        }
    }

    override fun cancelUpload() = uploadJob?.let {
        if (it.isActive) it.cancel()
    } ?: run { }

    override fun updateMessage(messageEntity: MessageEntity) {
        msgDataSource.updateMessage(messageEntity)
    }

    override suspend fun compressVideo(
        attachmentEntity: AttachmentEntity,
        srcFile: File,
        destFile: File,
        job: CoroutineScope
    ) = flow {
        if (attachmentEntity.mustBeCompressed()) {
            if (destFile.exists()) destFile.delete()
            VideoCompressK.compressVideoCustom(srcFile, destFile, job).collect { emit(it) }
        } else {
            emit(VideoCompressResult.Success(srcFile, destFile))
        }
    }

    override fun verifyMustMarkMessageAsSent() {
        coroutineScope.launch {
            val msgAndAttachments = msgDataSource.getMessageById(currentMessage.id, false)
            msgAndAttachments?.let {
                val attachmentsFilterBySend = it.attachmentEntityList.filter { it.isSent() }
                if (attachmentsFilterBySend.size == it.messageEntity.numberAttachments) {
                    val msgToUpdate = it.messageEntity.copy(status = MessageStatus.SENT.status)
                    msgDataSource.updateMessage(msgToUpdate)
                }
            }
            publishEventTryNext()
        }
    }

    override fun updateAttachment(attachmentEntity: AttachmentEntity) {
        attachmentDataSource.updateAttachment(attachmentEntity)
    }

    override fun tryMarkAttachmentsInMessageAsError(messageEntity: MessageEntity) {
        GlobalScope.launch {
            val msgAndRelation = msgDataSource.getMessageById(messageEntity.id, false)
            msgAndRelation?.let {msgAndRelation ->
                val attachmentsInCancelUpload = msgAndRelation.attachmentEntityList.filter {
                    it.isCancelUpload() || it.isSending()
                }
                attachmentsInCancelUpload.forEach { markAttachmentAsError(it) }
                updateMessageStatus(msgAndRelation.messageEntity, MessageStatus.ERROR.status)
                publishExitService()
            }
        }
    }

    private suspend fun handleVideoCompressResult(it: VideoCompressResult, job: Job) = when (it) {
        is VideoCompressResult.Start -> handleCompressStart()
        is VideoCompressResult.Success -> handleCompressSuccess(it, job)
        is VideoCompressResult.Progress -> handleCompressProgress(it)
        is VideoCompressResult.Fail -> publishEventError()
    }

    private fun handleCompressStart() = Timber.d("*Test: tmessages VideoCompressResult.Start")

    private suspend fun handleCompressSuccess(it: VideoCompressResult.Success, job: Job) {
        try {

            currentAttachment.apply {
                if (it.srcFile.isFile && it.srcFile.exists() && isCompressed.not() && type == AttachmentType.VIDEO.type) {
                    it.srcFile.delete()
                }

                fileName =
                    if (type == AttachmentType.VIDEO.type) {
                        it.destFile.name
                    } else {
                        it.srcFile.name
                    }

                isCompressed = true
            }
            updateAttachment(currentAttachment)

            currentAttachment.apply {

                val requestBodyMessageId = createPartFromString(messageWebId)
                val requestBodyType = createPartFromString(type)
                val requestBodyDuration = createPartFromString(duration.toString())
                val requestBodyDestroy = createPartFromString(this.selfDestructionAt.toString())
                val requestBodyUuid = createPartFromString(uuid ?: UUID.randomUUID().toString())

                val requestBodyFilePart = createPartFromFile(
                    this, job,
                    progress = { progress ->
                        //publishEventProgress(progress)
                        Timber.d("UploadResult.Progress($progress})")
                    }
                )

                val response = napoleonApi.sendMessageAttachment(
                    messageId = requestBodyMessageId,
                    attachmentType = requestBodyType,
                    duration = requestBodyDuration,
                    destroy = requestBodyDestroy,
                    uuid = requestBodyUuid,
                    file = requestBodyFilePart,
                )

                if (response.isSuccessful) {
                    currentAttachment.status = Constants.AttachmentStatus.SENT.status
                    updateAttachment(currentAttachment)
                    handleResponseSuccessful(response)
                } else {
                    publishEventError()
                }
            }
        } catch (exception: Exception) {
            publishEventError()
        }
    }

    private fun handleCompressProgress(it: VideoCompressResult.Progress) {
        Timber.d("VideoCompressResult.Progress ${it.progress}")
        //currentAttachment.publishEventProgress(it.progress)
    }

    private fun handleResponseSuccessful(response: Response<AttachmentResDTO>) {
        response.body()?.let { attachmentResDTO ->
            currentAttachment.apply {
                webId = attachmentResDTO.id
                messageWebId = attachmentResDTO.messageId
                body = attachmentResDTO.body
                status = Constants.AttachmentStatus.SENT.status
            }
            updateAttachment(currentAttachment)
            if (BuildConfig.ENCRYPT_API && currentAttachment.type != AttachmentType.GIF_NN.type) {
                saveEncryptedFile(currentAttachment)
            } else {
                verifyMustMarkMessageAsSent()
            }
        }
    }

    private fun saveEncryptedFile(attachmentEntity: AttachmentEntity) {
        FileManager.copyEncryptedFile(context, attachmentEntity)
        verifyMustMarkMessageAsSent()
    }

    private fun setStatusErrorMessageAndAttachment(
        messageEntity: MessageEntity,
        attachmentEntity: AttachmentEntity?
    ) {
        updateMessageStatus(messageEntity, MessageStatus.ERROR.status)
        attachmentEntity?.let {
            attachmentEntity.status = Constants.AttachmentStatus.ERROR.status
            updateAttachment(attachmentEntity)
        }
    }

    private fun createPartFromString(string: String): RequestBody =
        RequestBody.create(MultipartBody.FORM, string)

    private fun createPartFromFile(
        attachmentEntity: AttachmentEntity,
        job: Job,
        progress: (Float) -> Unit
    ): MultipartBody.Part {

        Timber.d("createPartFromFile")
        val subfolder =
            FileManager.getSubfolderByAttachmentType(attachmentType = attachmentEntity.type)

        val fileUri = Utils.getFileUri(
            context = context, fileName = attachmentEntity.fileName, subFolder = subfolder
        )

        val file = File(fileUri.path!!)
        val stream = context.contentResolver.openInputStream(fileUri)
        val byteArrayStream = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var i: Int

        while (stream!!.read(buffer, 0, buffer.size).also { i = it } > 0) {
            byteArrayStream.write(buffer, 0, i)
        }

        val byteArray = byteArrayStream.toByteArray()
        val extension = MimeTypeMap.getFileExtensionFromUrl(file.toString())
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        val mediaType = MediaType.parse(mimeType!!)
        val progressRequestBody = ProgressRequestBody(byteArray, mediaType!!,
            progress = { progress(it) }
        )

        Timber.d("before return MultiparBody, $job")
        return MultipartBody.Part.createFormData(
            "body",
            "${System.currentTimeMillis()}.$extension",
            progressRequestBody
        )
    }

    private fun getDestFileForCompress(attachmentEntity: AttachmentEntity): Pair<File, File> {
        val path = File(
            context.cacheDir!!,
            FileManager.getSubfolderByAttachmentType(attachmentEntity.type)
        )
        if (!path.exists()) path.mkdirs()
        val sourceFile = File(path, attachmentEntity.fileName)
        val child = "${
            attachmentEntity.fileName
                .replace("_compress", "")
                .split('.')[0]
        }_compress.${attachmentEntity.extension}"
        val destFile = File(path, child)
        return Pair(sourceFile, destFile)
    }

    private fun updateMessageStatus(messageEntity: MessageEntity, status: Int) {
        messageEntity.status = status
        updateMessage(messageEntity)
    }

    /**
     * Si ha fallado la subida, debemos actualizar el tiempo de destruccion del attachmente
     * debemos poner el valor por defecto seleccionado para mensajes de error
     */
    private suspend fun markAttachmentAsError(attachmentEntity: AttachmentEntity) {
        attachmentDataSource.markAttachmentAsError(attachmentEntity)
    }

    private fun publishEventTryNext() = RxBus.publish(RxEvent.MultiUploadTryNextAttachment())

    private fun publishEventStart() = RxBus.publish(RxEvent.MultiUploadStart(currentAttachment))

    private fun publishEventError() = RxBus.publish(
        RxEvent.MultiUploadError(
            currentMessage,
            currentAttachment,
            "Algo ha salido mal",
            null
        )
    )

    private fun publishExitService() = RxBus.publish(RxEvent.ExitOfService())

    private fun AttachmentEntity.publishEventProgress(
        progress: Float
    ) = RxBus.publish(
        RxEvent.MultiUploadProgress(this, progress)
    )
}