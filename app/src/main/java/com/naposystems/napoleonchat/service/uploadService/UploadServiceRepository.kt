package com.naposystems.napoleonchat.service.uploadService

import android.content.Context
import android.webkit.MimeTypeMap
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.source.local.datasource.attachment.AttachmentLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.message.MessageLocalDataSource
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.utility.*
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
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
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UploadServiceRepository @Inject constructor(
    private val context: Context,
    private val messageLocalDataSource: MessageLocalDataSource,
    private val attachmentLocalDataSource: AttachmentLocalDataSource,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val napoleonApi: NapoleonApi
) : IContractUploadService.Repository {

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.IO)
    private var uploadJob: Job? = null

    override fun uploadAttachment(
        attachmentEntity: AttachmentEntity,
        messageEntity: MessageEntity
    ) {
        uploadJob = coroutineScope.launch {
            try {
                updateAttachment(attachmentEntity)
                messageEntity.status = Constants.MessageStatus.SENDING.status
                updateMessage(messageEntity)
                //offer(UploadResult.Start(attachment, this))
                RxBus.publish(RxEvent.UploadStart(attachmentEntity))
                Timber.d("UploadResult.Start(attachment, this)")

                val path =
                    File(
                        context.cacheDir!!,
                        FileManager.getSubfolderByAttachmentType(attachmentEntity.type)
                    )
                if (!path.exists())
                    path.mkdirs()
                val sourceFile = File(path, attachmentEntity.fileName)
                val destFile =
                    File(
                        path, "${
                            attachmentEntity.fileName
                                .replace("_compress", "")
                                .split('.')[0]
                        }_compress.${attachmentEntity.extension}"
                    )

                compressVideo(attachmentEntity, sourceFile, destFile, this)
                    .collect {
                        when (it) {
                            is VideoCompressResult.Start -> {
                                Timber.d("*Test: tmessages VideoCompressResult.Start")
                            }
                            is VideoCompressResult.Success -> {
                                Timber.d("*Test: tmessages VideoCompressResult.Success")
                                if (it.srcFile.isFile && it.srcFile.exists() && !attachmentEntity.isCompressed && attachmentEntity.type == Constants.AttachmentType.VIDEO.type)
                                    it.srcFile.delete()
                                attachmentEntity.fileName =
                                    if (attachmentEntity.type == Constants.AttachmentType.VIDEO.type) it.destFile.name else it.srcFile.name
                                attachmentEntity.isCompressed = true
                                updateAttachment(attachmentEntity)

                                val requestBodyMessageId =
                                    createPartFromString(attachmentEntity.messageWebId)
                                val requestBodyType = createPartFromString(attachmentEntity.type)
                                val requestBodyDuration =
                                    createPartFromString(attachmentEntity.duration.toString())

                                val requestBodyFilePart =
                                    createPartFromFile(
                                        attachmentEntity,
                                        this as Job,
                                        progress = { progress ->
                                            //offer(UploadResult.Progress(attachment, progress, this))
                                            RxBus.publish(
                                                RxEvent.UploadProgress(
                                                    attachmentEntity,
                                                    progress
                                                )
                                            )
                                            Timber.d("UploadResult.Progress($progress})")
                                        }
                                    )

                                val response = napoleonApi.sendMessageAttachment(
                                    messageId = requestBodyMessageId,
                                    attachmentType = requestBodyType,
                                    duration = requestBodyDuration,
                                    file = requestBodyFilePart
                                )

                                if (response.isSuccessful) {

                                    messageEntity.status =
                                        if (messageEntity.isMine == Constants.IsMine.NO.value) Constants.MessageStatus.UNREAD.status
                                        else Constants.MessageStatus.SENT.status
                                    updateMessage(messageEntity)

                                    response.body()?.let { attachmentResDTO ->
                                        attachmentEntity.apply {
                                            webId = attachmentResDTO.id
                                            messageWebId = attachmentResDTO.messageId
                                            body = attachmentResDTO.body
                                            status = Constants.AttachmentStatus.SENT.status
                                        }
                                    }

                                    updateAttachment(attachmentEntity)
                                    if (BuildConfig.ENCRYPT_API && attachmentEntity.type != Constants.AttachmentType.GIF_NN.type) {
                                        saveEncryptedFile(attachmentEntity)
                                    }
                                    //offer(UploadResult.Success(attachment))
                                    RxBus.publish(RxEvent.UploadSuccess(attachmentEntity))
                                    Timber.d("offer(UploadResult.Success(attachment))")
                                } else {
                                    setStatusErrorMessageAndAttachment(messageEntity, attachmentEntity)
                                    //offer(UploadResult.Error(attachment, "Algo ha salido mal", null))
                                    RxBus.publish(
                                        RxEvent.UploadError(
                                            attachmentEntity,
                                            "Algo ha salido mal",
                                            null
                                        )
                                    )
                                    Timber.d("offer(UploadResult.Error(attachment, \"Algo ha salido mal\", null))")
                                }
                            }
                            is VideoCompressResult.Progress -> {
                                Timber.d("VideoCompressResult.Progress ${it.progress}")
                                //offer(UploadResult.CompressProgress(attachment, it.progress, this))
                                RxBus.publish(RxEvent.CompressProgress(attachmentEntity, it.progress))
                            }
                            is VideoCompressResult.Fail -> {
                                setStatusErrorMessageAndAttachment(messageEntity, attachmentEntity)
                                //offer(UploadResult.Error(attachment, "Algo ha salido mal", null))
                                RxBus.publish(
                                    RxEvent.UploadError(
                                        attachmentEntity,
                                        "Algo ha salido mal",
                                        null
                                    )
                                )
                                Timber.d("offer(UploadResult.Error(attachment, \"Algo ha salido mal\", null))")
                            }
                        }
                    }
            } catch (e: Exception) {
                Timber.e("ClosedSendChannelException, $e")
                attachmentEntity.status =
                    Constants.AttachmentStatus.UPLOAD_CANCEL.status
                updateAttachment(attachmentEntity)

                messageEntity.status = Constants.MessageStatus.ERROR.status
                updateMessage(messageEntity)
                RxBus.publish(
                    RxEvent.UploadError(
                        attachmentEntity,
                        "Algo ha salido mal",
                        null
                    )
                )
            }
        }
    }

    override fun cancelUpload() {
        if (uploadJob?.isActive == true) {
            uploadJob?.cancel()
        }
    }

    override fun updateAttachment(attachmentEntity: AttachmentEntity) {
        Timber.d("update attachment")
        attachmentLocalDataSource.updateAttachment(attachmentEntity)
    }

    override fun updateMessage(messageEntity: MessageEntity) {
        Timber.d("updateMessage")
        when (messageEntity.status) {
            Constants.MessageStatus.ERROR.status -> {
                val selfDestructTime = sharedPreferencesManager.getInt(
                    Constants.SharedPreferences.PREF_MESSAGE_SELF_DESTRUCT_TIME_NOT_SENT
                )
                val currentTime =
                    TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toInt()
                messageEntity.updatedAt = currentTime
                messageEntity.selfDestructionAt = selfDestructTime
                messageEntity.totalSelfDestructionAt =
                    currentTime.plus(Utils.convertItemOfTimeInSecondsByError(selfDestructTime))
                messageLocalDataSource.updateMessage(messageEntity)
            }
            else -> {
                messageLocalDataSource.updateMessage(messageEntity)
            }
        }
    }

    override suspend fun compressVideo(
        attachmentEntity: AttachmentEntity,
        srcFile: File,
        destFile: File,
        job: CoroutineScope
    ) = flow {

        if (attachmentEntity.type == Constants.AttachmentType.VIDEO.type && !attachmentEntity.isCompressed) {
            if (destFile.exists())
                destFile.delete()

            VideoCompressK.compressVideoCustom(
                srcFile, destFile, job
            ).collect {
                emit(it)
            }
        } else {
            emit(VideoCompressResult.Success(srcFile, destFile))
        }
    }

    private fun createPartFromString(string: String): RequestBody {
        return RequestBody.create(MultipartBody.FORM, string)
    }

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

        val progressRequestBody =
            ProgressRequestBody(
                byteArray,
                mediaType!!,
                progress = {
                    progress(it)
                }
            )

        Timber.d("before return MultiparBody, $job")
        return MultipartBody.Part.createFormData(
            "body",
            "${System.currentTimeMillis()}.$extension",
            progressRequestBody
        )
    }

    private fun saveEncryptedFile(attachmentEntity: AttachmentEntity) {
        FileManager.copyEncryptedFile(context, attachmentEntity)
    }

    private fun setStatusErrorMessageAndAttachment(messageEntity: MessageEntity, attachmentEntity: AttachmentEntity?) {
        messageEntity.status = Constants.MessageStatus.ERROR.status
        updateMessage(messageEntity)
        attachmentEntity?.let {
            attachmentEntity.status = Constants.AttachmentStatus.ERROR.status
            updateAttachment(attachmentEntity)
        }
    }
}