package com.naposystems.napoleonchat.service.multiattachment

import android.content.Context
import android.webkit.MimeTypeMap
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.source.local.datasource.attachment.AttachmentLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.message.MessageLocalDataSource
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.conversation.attachment.AttachmentResDTO
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Constants.AttachmentType
import com.naposystems.napoleonchat.utility.Constants.IsMine
import com.naposystems.napoleonchat.utility.Constants.MessageStatus
import com.naposystems.napoleonchat.utility.Constants.MessageStatus.SENDING
import com.naposystems.napoleonchat.utility.Constants.MessageStatus.UNREAD
import com.naposystems.napoleonchat.utility.Constants.SharedPreferences.PREF_MESSAGE_SELF_DESTRUCT_TIME_NOT_SENT
import com.naposystems.napoleonchat.utility.FileManager
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.webService.ProgressRequestBody
import com.vincent.videocompressor.VideoCompressK
import com.vincent.videocompressor.VideoCompressResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
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
                updateAttachment(attachmentEntity)
                updateMessageStatus(messageEntity, SENDING.status)
                publishEventStart()
                val pairFiles = getDestFileForCompress(attachmentEntity)

                compressVideo(attachmentEntity, pairFiles.first, pairFiles.second, this)
                    .collect { handleVideoCompressResult(it, this as Job) }

            } catch (exception: Exception) {
                handleExceptionInUploadAttachment()
            }
        }
    }

    override fun cancelUpload() = uploadJob?.let {
        if (it.isActive) it.cancel()
    } ?: run { }

    override fun updateAttachment(attachmentEntity: AttachmentEntity) =
        attachmentDataSource.updateAttachment(attachmentEntity)

    override fun updateMessage(messageEntity: MessageEntity) {
        Timber.d("updateMessage")
        when (messageEntity.status) {
            MessageStatus.ERROR.status -> handleMessageStatusError(messageEntity)
            else -> msgDataSource.updateMessage(messageEntity)
        }
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

    private suspend fun handleVideoCompressResult(it: VideoCompressResult, job: Job) = when (it) {
        is VideoCompressResult.Start -> handleCompressStart()
        is VideoCompressResult.Success -> handleCompressSuccess(it, job)
        is VideoCompressResult.Progress -> handleCompressProgress(it)
        is VideoCompressResult.Fail -> handleCompressFailure()
    }

    private fun handleCompressStart() = Timber.d("*Test: tmessages VideoCompressResult.Start")

    private suspend fun handleCompressSuccess(it: VideoCompressResult.Success, job: Job) {
        Timber.d("*Test: tmessages VideoCompressResult.Success")

        currentAttachment.apply {
            if (it.srcFile.isFile && it.srcFile.exists() && !isCompressed && type == AttachmentType.VIDEO.type)
                it.srcFile.delete()
            fileName = if (type == AttachmentType.VIDEO.type) it.destFile.name else it.srcFile.name
            isCompressed = true
            updateAttachment(this)
        }

        currentAttachment.apply {

            val requestBodyMessageId = createPartFromString(messageWebId)
            val requestBodyType = createPartFromString(type)
            val requestBodyDuration = createPartFromString(duration.toString())
            val requestBodyDestroy = createPartFromString(7.toString())

            val requestBodyFilePart = createPartFromFile(
                this, job,
                progress = { progress ->
                    publishEventProgress(progress)
                    Timber.d("UploadResult.Progress($progress})")
                }
            )

            val response = napoleonApi.sendMessageAttachment(
                messageId = requestBodyMessageId,
                attachmentType = requestBodyType,
                duration = requestBodyDuration,
                destroy = requestBodyDestroy,
                file = requestBodyFilePart
            )

            if (response.isSuccessful) {
                handleResponseSuccessful(response)
            } else {
                handleResponseFailure()
            }
        }
    }

    private fun handleCompressProgress(it: VideoCompressResult.Progress) {
        Timber.d("VideoCompressResult.Progress ${it.progress}")
        currentAttachment.publishEventProgress(it.progress)
    }

    private fun handleCompressFailure() {
        setStatusErrorMessageAndAttachment(currentMessage, currentAttachment)
        publishEventError()
        Timber.d("offer(UploadResult.Error(attachment, \"Algo ha salido mal\", null))")
    }

    private fun handleResponseFailure() {
        setStatusErrorMessageAndAttachment(currentMessage, currentAttachment)
        publishEventError()
        Timber.d("offer(UploadResult.Error(attachment, \"Algo ha salido mal\", null))")
    }

    private fun handleResponseSuccessful(response: Response<AttachmentResDTO>) {

        currentMessage.apply {
            status = if (currentMessage.isMine == IsMine.NO.value) UNREAD.status
            else MessageStatus.SENT.status
            updateMessage(currentMessage)
        }

        response.body()?.let { attachmentResDTO ->
            currentAttachment.apply {
                webId = attachmentResDTO.id
                messageWebId = attachmentResDTO.messageId
                body = attachmentResDTO.body
                status = Constants.AttachmentStatus.SENT.status
            }
        }

        currentAttachment.apply {
            updateAttachment(this)
            if (BuildConfig.ENCRYPT_API && type != AttachmentType.GIF_NN.type) {
                saveEncryptedFile(this)
            }
            publishEventTryNext()
        }
    }

    private fun saveEncryptedFile(attachmentEntity: AttachmentEntity) =
        FileManager.copyEncryptedFile(context, attachmentEntity)

    private fun setStatusErrorMessageAndAttachment(
        messageEntity: MessageEntity,
        attachmentEntity: AttachmentEntity?
    ) {
        messageEntity.status = MessageStatus.ERROR.status
        updateMessage(messageEntity)
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
        val path =
            File(
                context.cacheDir!!,
                FileManager.getSubfolderByAttachmentType(attachmentEntity.type)
            )
        if (!path.exists())
            path.mkdirs()
        val sourceFile = File(path, attachmentEntity.fileName)
        val destFile = File(
            path, "${
                attachmentEntity.fileName
                    .replace("_compress", "")
                    .split('.')[0]
            }_compress.${attachmentEntity.extension}"
        )
        return Pair(sourceFile, destFile)
    }

    private fun updateMessageStatus(messageEntity: MessageEntity, status: Int) {
        messageEntity.status = status
        updateMessage(messageEntity)
    }

    private fun handleMessageStatusError(messageEntity: MessageEntity) {
        val selfDestructTime = preferences.getInt(PREF_MESSAGE_SELF_DESTRUCT_TIME_NOT_SENT)
        val currentTime = MILLISECONDS.toSeconds(System.currentTimeMillis()).toInt()
        messageEntity.apply {
            updatedAt = currentTime
            selfDestructionAt = selfDestructTime
            totalSelfDestructionAt =
                currentTime.plus(Utils.convertItemOfTimeInSecondsByError(selfDestructTime))
            msgDataSource.updateMessage(this)
        }
    }

    private fun handleExceptionInUploadAttachment() {
        currentAttachment.apply {
            status = Constants.AttachmentStatus.UPLOAD_CANCEL.status
            updateAttachment(this)
        }
        currentMessage.apply {
            status = MessageStatus.ERROR.status
            updateMessage(this)
        }
        publishEventError()
    }

    private fun publishEventTryNext() {
        RxBus.publish(RxEvent.MultiUploadTryNextAttachment())
        Timber.d("offer(UploadResult.Success(attachment))")
    }

    private fun publishEventSuccess() {
        RxBus.publish(RxEvent.MultiUploadSuccess(currentAttachment))
        Timber.d("offer(UploadResult.Success(attachment))")
    }

    private fun publishEventStart() = RxBus.publish(RxEvent.MultiUploadStart(currentAttachment))

    private fun publishEventError() = RxBus.publish(
        RxEvent.MultiUploadError(currentAttachment, "Algo ha salido mal", null)
    )

    private fun AttachmentEntity.publishEventProgress(
        progress: Float
    ) = RxBus.publish(
        RxEvent.MultiUploadProgress(this, progress)
    )
}