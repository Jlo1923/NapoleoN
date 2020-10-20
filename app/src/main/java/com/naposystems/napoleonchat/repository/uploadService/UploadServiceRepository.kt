package com.naposystems.napoleonchat.repository.uploadService

import android.content.Context
import android.webkit.MimeTypeMap
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.db.dao.attachment.AttachmentDataSource
import com.naposystems.napoleonchat.db.dao.message.MessageDataSource
import com.naposystems.napoleonchat.entity.message.Message
import com.naposystems.napoleonchat.entity.message.attachments.Attachment
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.uploadService.IContractUploadService
import com.naposystems.napoleonchat.utility.*
import com.naposystems.napoleonchat.webService.NapoleonApi
import com.naposystems.napoleonchat.webService.ProgressRequestBody
import com.vincent.videocompressor.VideoCompressK
import com.vincent.videocompressor.VideoCompressResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.channelFlow
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
    private val messageLocalDataSource: MessageDataSource,
    private val attachmentLocalDataSource: AttachmentDataSource,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val napoleonApi: NapoleonApi
) : IContractUploadService.Repository {

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.IO)

    override fun uploadAttachment(
        attachment: Attachment,
        message: Message
    ) {
        coroutineScope.launch {
            try {
                updateAttachment(attachment)
                message.status = Constants.MessageStatus.SENDING.status
                updateMessage(message)
                //offer(UploadResult.Start(attachment, this))
                RxBus.publish(RxEvent.UploadStart(attachment))
                Timber.d("UploadResult.Start(attachment, this)")

                val path =
                    File(
                        context.cacheDir!!,
                        FileManager.getSubfolderByAttachmentType(attachment.type)
                    )
                if (!path.exists())
                    path.mkdirs()
                val sourceFile = File(path, attachment.fileName)
                val destFile =
                    File(
                        path, "${
                            attachment.fileName
                                .replace("_compress", "")
                                .split('.')[0]
                        }_compress.${attachment.extension}"
                    )

                compressVideo(attachment, sourceFile, destFile, this)
                    .collect {
                        when (it) {
                            is VideoCompressResult.Start -> {
                                Timber.d("*Test: tmessages VideoCompressResult.Start")
                            }
                            is VideoCompressResult.Success -> {
                                Timber.d("*Test: tmessages VideoCompressResult.Success")
                                if (it.srcFile.isFile && it.srcFile.exists() && !attachment.isCompressed && attachment.type == Constants.AttachmentType.VIDEO.type)
                                    it.srcFile.delete()
                                attachment.fileName =
                                    if (attachment.type == Constants.AttachmentType.VIDEO.type) it.destFile.name else it.srcFile.name
                                attachment.isCompressed = true
                                updateAttachment(attachment)

                                val requestBodyMessageId =
                                    createPartFromString(attachment.messageWebId)
                                val requestBodyType = createPartFromString(attachment.type)
                                val requestBodyDuration =
                                    createPartFromString(attachment.duration.toString())

                                val requestBodyFilePart =
                                    createPartFromFile(
                                        attachment,
                                        this as Job,
                                        progress = { progress ->
                                            //offer(UploadResult.Progress(attachment, progress, this))
                                            RxBus.publish(
                                                RxEvent.UploadProgress(
                                                    attachment,
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

                                    message.status =
                                        if (message.isMine == Constants.IsMine.NO.value) Constants.MessageStatus.UNREAD.status
                                        else Constants.MessageStatus.SENT.status
                                    updateMessage(message)

                                    response.body()?.let { attachmentResDTO ->
                                        attachment.apply {
                                            webId = attachmentResDTO.id
                                            messageWebId = attachmentResDTO.messageId
                                            body = attachmentResDTO.body
                                            status = Constants.AttachmentStatus.SENT.status
                                        }
                                    }

                                    updateAttachment(attachment)
                                    if (BuildConfig.ENCRYPT_API && attachment.type != Constants.AttachmentType.GIF_NN.type) {
                                        saveEncryptedFile(attachment)
                                    }
                                    //offer(UploadResult.Success(attachment))
                                    RxBus.publish(RxEvent.UploadSuccess(attachment))
                                    Timber.d("offer(UploadResult.Success(attachment))")
                                } else {
                                    setStatusErrorMessageAndAttachment(message, attachment)
                                    //offer(UploadResult.Error(attachment, "Algo ha salido mal", null))
                                    RxBus.publish(
                                        RxEvent.UploadError(
                                            attachment,
                                            "Algo ha salido mal",
                                            null
                                        )
                                    )
                                    Timber.d("offer(UploadResult.Error(attachment, \"Algo ha salido mal\", null))")
                                }
                            }
                            is VideoCompressResult.Progress -> {
                                //Timber.d("VideoCompressResult.Progress ${it.progress}")
                                //offer(UploadResult.CompressProgress(attachment, it.progress, this))
                                RxBus.publish(RxEvent.CompressProgress(attachment, it.progress))
                            }
                            is VideoCompressResult.Fail -> {
                                setStatusErrorMessageAndAttachment(message, attachment)
                                //offer(UploadResult.Error(attachment, "Algo ha salido mal", null))
                                RxBus.publish(
                                    RxEvent.UploadError(
                                        attachment,
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
                attachment.status =
                    Constants.AttachmentStatus.UPLOAD_CANCEL.status
                updateAttachment(attachment)

                message.status = Constants.MessageStatus.ERROR.status
                updateMessage(message)
            }
        }
    }

    override fun updateAttachment(attachment: Attachment) {
        Timber.d("update attachment")
        attachmentLocalDataSource.updateAttachment(attachment)
    }

    override fun updateMessage(message: Message) {
        Timber.d("updateMessage")
        when (message.status) {
            Constants.MessageStatus.ERROR.status -> {
                val selfDestructTime = sharedPreferencesManager.getInt(
                    Constants.SharedPreferences.PREF_MESSAGE_SELF_DESTRUCT_TIME_NOT_SENT
                )
                val currentTime =
                    TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toInt()
                message.updatedAt = currentTime
                message.selfDestructionAt = selfDestructTime
                message.totalSelfDestructionAt =
                    currentTime.plus(Utils.convertItemOfTimeInSecondsByError(selfDestructTime))
                messageLocalDataSource.updateMessage(message)
            }
            else -> {
                messageLocalDataSource.updateMessage(message)
            }
        }
    }

    override suspend fun compressVideo(
        attachment: Attachment,
        srcFile: File,
        destFile: File,
        job: CoroutineScope
    ) = flow {

        if (attachment.type == Constants.AttachmentType.VIDEO.type && !attachment.isCompressed) {
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
        attachment: Attachment,
        job: Job,
        progress: (Float) -> Unit
    ): MultipartBody.Part {
        Timber.d("createPartFromFile")
        val subfolder =
            FileManager.getSubfolderByAttachmentType(attachmentType = attachment.type)

        val fileUri = Utils.getFileUri(
            context = context, fileName = attachment.fileName, subFolder = subfolder
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

    private fun saveEncryptedFile(attachment: Attachment) {
        FileManager.copyEncryptedFile(context, attachment)
    }

    private fun setStatusErrorMessageAndAttachment(message: Message, attachment: Attachment?) {
        message.status = Constants.MessageStatus.ERROR.status
        updateMessage(message)
        attachment?.let {
            attachment.status = Constants.AttachmentStatus.ERROR.status
            updateAttachment(attachment)
        }
    }
}