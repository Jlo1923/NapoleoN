package com.naposystems.napoleonchat.ui.conversation

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.database.getStringOrNull
import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.socketClient.SocketClient
import com.naposystems.napoleonchat.source.local.datasource.attachment.AttachmentLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.message.MessageLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.messageNotSent.MessageNotSentLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.quoteMessage.QuoteLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.user.UserLocalDataSource
import com.naposystems.napoleonchat.source.local.entity.*
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.conversation.deleteMessages.DeleteMessageUnprocessableEntityDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.deleteMessages.DeleteMessagesErrorDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.deleteMessages.DeleteMessagesReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.deleteMessages.DeleteMessagesResDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.*
import com.naposystems.napoleonchat.source.remote.dto.validateMessageEvent.ValidateMessage
import com.naposystems.napoleonchat.utility.*
import com.naposystems.napoleonchat.webService.ProgressRequestBody
import com.squareup.moshi.Moshi
import com.vincent.videocompressor.VideoCompressK
import com.vincent.videocompressor.VideoCompressResult
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import timber.log.Timber
import java.io.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ConversationRepository @Inject constructor(
    private val context: Context,
    private val socketClient: SocketClient,
    private val userLocalDataSource: UserLocalDataSource,
    private val messageLocalDataSource: MessageLocalDataSource,
    private val attachmentLocalDataSource: AttachmentLocalDataSource,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val napoleonApi: NapoleonApi,
    private val quoteLocalDataSource: QuoteLocalDataSource,
    private val messageNotSentLocalDataSource: MessageNotSentLocalDataSource
) : IContractConversation.Repository {

    private var envioEnProceso: Boolean = true

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    private val firebaseId: String by lazy {
        sharedPreferencesManager
            .getString(Constants.SharedPreferences.PREF_FIREBASE_ID, "")
    }

    override fun getLocalMessages(contactId: Int): LiveData<List<MessageAttachmentRelation>> {
        return messageLocalDataSource.getMessages(contactId)
    }

    override suspend fun getQuoteId(quoteWebId: String): Int {
        return messageLocalDataSource.getQuoteId(quoteWebId)
    }

    override fun getLocalMessagesByStatus(
        contactId: Int,
        status: Int
    ): List<MessageAttachmentRelation> {
        return messageLocalDataSource.getLocalMessagesByStatus(contactId, status)
    }

    override suspend fun sendMessage(messageReqDTO: MessageReqDTO): Response<MessageResDTO> {
        return napoleonApi.sendMessage(messageReqDTO)
    }

    override suspend fun uploadAttachment(
        attachmentEntity: AttachmentEntity,
        messageEntity: MessageEntity
    ) = channelFlow {
        try {
            updateAttachment(attachmentEntity)
            messageEntity.status = Constants.MessageStatus.SENDING.status
            updateMessage(messageEntity)
            offer(UploadResult.Start(attachmentEntity, this))

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
                                        offer(
                                            UploadResult.Progress(
                                                attachmentEntity,
                                                progress,
                                                this
                                            )
                                        )
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
                                offer(UploadResult.Success(attachmentEntity))
                            } else {
                                setStatusErrorMessageAndAttachment(messageEntity, attachmentEntity)
                                offer(
                                    UploadResult.Error(
                                        attachmentEntity,
                                        "Algo ha salido mal",
                                        null
                                    )
                                )
                            }
                        }
                        is VideoCompressResult.Progress -> {
                            Timber.d("tmessages VideoCompressResult.Progress ${it.progress}")
                            offer(
                                UploadResult.CompressProgress(
                                    attachmentEntity,
                                    it.progress,
                                    this
                                )
                            )
                        }
                        is VideoCompressResult.Fail -> {
                            setStatusErrorMessageAndAttachment(messageEntity, attachmentEntity)
                            offer(UploadResult.Error(attachmentEntity, "Algo ha salido mal", null))
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
        }
    }

    private fun saveEncryptedFile(attachmentEntity: AttachmentEntity) {
        FileManager.copyEncryptedFile(context, attachmentEntity)
    }

    private fun setStatusErrorMessageAndAttachment(
        messageEntity: MessageEntity,
        attachmentEntity: AttachmentEntity?
    ) {
        messageEntity.status = Constants.MessageStatus.ERROR.status
        updateMessage(messageEntity)
        attachmentEntity?.let {
            attachmentEntity.status = Constants.AttachmentStatus.ERROR.status
            updateAttachment(attachmentEntity)
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
                progress = { progress ->
                    progress(progress)
                }
            )

        Timber.d("before return MultiparBody, $job")
        return MultipartBody.Part.createFormData(
            "body",
            "${System.currentTimeMillis()}.$extension",
            progressRequestBody
        )
    }

    override suspend fun getLocalUser(): UserEntity {
        return userLocalDataSource.getMyUser()
    }

    override suspend fun insertMessage(messageEntity: MessageEntity): Long {
        return messageLocalDataSource.insertMessage(messageEntity)
    }

    override fun insertListMessage(messageEntityList: List<MessageEntity>) {
        messageLocalDataSource.insertListMessage(messageEntityList)
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

    override suspend fun sendTextMessagesRead(contactId: Int) {

        Timber.d("Envio en proceso Inicio $envioEnProceso")

        if (envioEnProceso) {

            envioEnProceso = false

            Timber.d("Envio en proceso dentro del IF $envioEnProceso")

            val messagesUnread =
                messageLocalDataSource.getTextMessagesByStatus(
                    contactId,
                    Constants.MessageStatus.UNREAD.status
                )

            val textMessagesUnread = messagesUnread.filter { it.attachmentEntityList.isEmpty() }

            val locationMessagesUnread =
                messagesUnread.filter { it.getFirstAttachment()?.type == Constants.AttachmentType.LOCATION.type }

            val textMessagesUnreadIds = textMessagesUnread.map { it.messageEntity.webId }

            val locationMessagesUnreadIds = locationMessagesUnread.map { it.messageEntity.webId }

            val listIds = mutableListOf<String>()

            listIds.addAll(textMessagesUnreadIds)

            listIds.addAll(locationMessagesUnreadIds)

            val messagesRead = messagesUnread.map {
                ValidateMessage(
                    id = it.messageEntity.webId,
                    user = contactId,
                    status = Constants.MessageEventType.READ.status
                )
            }

            if (listIds.isNotEmpty()) {

                try {

                    Timber.d("SocketService: $socketClient")

                    socketClient.emitClientConversation(messagesRead)

                    val response = napoleonApi.sendMessagesRead(
                        MessagesReadReqDTO(
                            listIds
                        )
                    )

                    if (response.isSuccessful) {

                        envioEnProceso = true

                        Timber.d("Envio en proceso Successful $envioEnProceso")

                        messageLocalDataSource.updateMessageStatus(
                            listIds,
                            Constants.MessageStatus.READED.status
                        )
                    }

                } catch (ex: Exception) {

                    envioEnProceso = true

                    Timber.d("Envio en proceso dentro del Catch $envioEnProceso")

                    Timber.e(ex)
                } finally {

                    envioEnProceso = true

                    Timber.d("Envio en proceso dentro del Finally $envioEnProceso")

                }

            } else {
                envioEnProceso = true
            }

        }

    }

    override suspend fun sendMissedCallRead(contactId: Int) {
        val messagesUnread =
            messageLocalDataSource.getMissedCallsByStatus(
                contactId,
                Constants.MessageStatus.UNREAD.status
            )

        val textMessagesUnread = messagesUnread.filter { it.attachmentEntityList.isEmpty() }
        val textMessagesUnreadIds = textMessagesUnread.map { it.messageEntity.webId }

        if (textMessagesUnreadIds.isNotEmpty()) {
            try {
                val response = napoleonApi.sendMessagesRead(
                    MessagesReadReqDTO(
                        textMessagesUnreadIds
                    )
                )

                if (response.isSuccessful) {
                    messageLocalDataSource.updateMessageStatus(
                        textMessagesUnreadIds,
                        Constants.MessageStatus.READED.status
                    )
                }
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    override fun insertAttachment(attachmentEntity: AttachmentEntity): Long {
        return attachmentLocalDataSource.insertAttachment(attachmentEntity)
    }

    override fun insertAttachments(listAttachmentEntity: List<AttachmentEntity>): List<Long> {
        return attachmentLocalDataSource.insertAttachments(listAttachmentEntity)
    }

    override fun updateAttachment(attachmentEntity: AttachmentEntity) {
        Timber.d("update attachment")
        attachmentLocalDataSource.updateAttachment(attachmentEntity)
    }

    override suspend fun suspendUpdateAttachment(attachmentEntity: AttachmentEntity) {
        attachmentLocalDataSource.suspendUpdateAttachment(attachmentEntity)
    }

    override suspend fun insertQuote(quoteWebId: String, messageEntity: MessageEntity) {

        val originalMessage =
            messageLocalDataSource.getMessageByWebId(quoteWebId, false)

        if (originalMessage != null) {
            var firstAttachmentEntity: AttachmentEntity? = null

            if (originalMessage.attachmentEntityList.isNotEmpty()) {
                firstAttachmentEntity = originalMessage.attachmentEntityList.first()
            }

            val quote = QuoteEntity(
                id = 0,
                messageId = messageEntity.id,
                contactId = originalMessage.messageEntity.contactId,
                body = originalMessage.messageEntity.body,
                attachmentType = firstAttachmentEntity?.type ?: "",
                thumbnailUri = firstAttachmentEntity?.fileName ?: "",
                messageParentId = originalMessage.messageEntity.id,
                isMine = originalMessage.messageEntity.isMine
            )

            quoteLocalDataSource.insertQuote(quote)
        }
    }

    override suspend fun updateStateSelectionMessage(
        contactId: Int,
        idMessage: Int,
        isSelected: Int
    ) {
        messageLocalDataSource.updateStateSelectionMessage(contactId, idMessage, isSelected)
    }

    override suspend fun cleanSelectionMessages(contactId: Int) {
        messageLocalDataSource.cleanSelectionMessages(contactId)
    }

    override suspend fun deleteMessagesSelected(
        contactId: Int,
        listMessageRelations: List<MessageAttachmentRelation>
    ) {
        listMessageRelations.filter { messageAndAttachment ->
            messageAndAttachment.attachmentEntityList.count() > 0 &&
                    messageAndAttachment.attachmentEntityList[0].type == Constants.AttachmentType.AUDIO.type
        }.let { listMessagesFiltered ->
            if (listMessagesFiltered.count() > 0) {
                RxBus.publish(RxEvent.MessagesToEliminate(listMessagesFiltered))
            }
        }
        messageLocalDataSource.deleteMessagesSelected(contactId, listMessageRelations)
    }

    override suspend fun deleteMessagesForAll(deleteMessagesReqDTO: DeleteMessagesReqDTO): Response<DeleteMessagesResDTO> {
        return napoleonApi.deleteMessagesForAll(deleteMessagesReqDTO)
    }

    override suspend fun copyMessagesSelected(contactId: Int): List<String> {
        return messageLocalDataSource.copyMessagesSelected(contactId)
    }

    override suspend fun getMessagesSelected(contactId: Int): LiveData<List<MessageAttachmentRelation>> {
        return messageLocalDataSource.getMessagesSelected(contactId)
    }

    override suspend fun deleteMessagesByStatusForMe(contactId: Int, status: Int) {
        messageLocalDataSource.deleteMessagesByStatusForMe(contactId, status)
    }

    override fun getUnprocessableEntityErrorMessage(response: Response<MessageResDTO>): ArrayList<String> {
        val adapter = moshi.adapter(MessageUnprocessableEntityDTO::class.java)

        val conversationError = adapter.fromJson(response.errorBody()!!.string())

        return WebServiceUtils.getUnprocessableEntityErrors(conversationError!!)
    }

    override fun getErrorMessage(response: Response<MessageResDTO>): ArrayList<String> {

        val adapter = moshi.adapter(MessageErrorDTO::class.java)

        val conversationError = adapter.fromJson(response.errorBody()!!.string())

        val errorList = ArrayList<String>()

        errorList.add(conversationError!!.error)

        return errorList
    }

    override fun getUnprocessableEntityErrorDeleteMessagesForAll(response: ResponseBody): ArrayList<String> {
        val adapter = moshi.adapter(DeleteMessageUnprocessableEntityDTO::class.java)

        val conversationError = adapter.fromJson(response.string())

        return WebServiceUtils.getUnprocessableEntityErrors(conversationError!!)
    }

    override fun getErrorDeleteMessagesForAll(response: ResponseBody): ArrayList<String> {
        val adapter = moshi.adapter(DeleteMessagesErrorDTO::class.java)

        val conversationError = adapter.fromJson(response.string())

        val errorList = ArrayList<String>()

        errorList.add(conversationError!!.error)

        return errorList
    }


    override suspend fun downloadAttachment(
        messageAndAttachmentRelation: MessageAttachmentRelation,
        itemPosition: Int
    ): Flow<DownloadAttachmentResult> = channelFlow<DownloadAttachmentResult> {
        messageAndAttachmentRelation.getFirstAttachment()?.let { attachment ->
            val fileName = "${System.currentTimeMillis()}.${attachment.extension}"
            /*val fileName = "${System.currentTimeMillis()}.${attachment.extension}"
            attachment.status = Constants.AttachmentStatus.DOWNLOADING.status
            attachment.uri = fileName
            Timber.d("Attachment status: ${attachment.status}, uri: ${attachment.uri}")
            updateAttachment(attachment)
            offer(DownloadAttachmentResult.Start(itemPosition, this))*/

            try {
                val response = napoleonApi.downloadFileByUrl(attachment.body)

                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        var folder = ""

                        when (attachment.type) {
                            Constants.AttachmentType.IMAGE.type,
                            Constants.AttachmentType.LOCATION.type -> {
                                folder =
                                    Constants.CacheDirectories.IMAGES.folder
                            }
                            Constants.AttachmentType.AUDIO.type -> {
                                folder =
                                    Constants.CacheDirectories.AUDIOS.folder
                            }
                            Constants.AttachmentType.VIDEO.type -> {
                                folder =
                                    Constants.CacheDirectories.VIDEOS.folder
                            }
                            Constants.AttachmentType.DOCUMENT.type -> {
                                folder =
                                    Constants.CacheDirectories.DOCUMENTOS.folder
                            }
                            Constants.AttachmentType.GIF.type -> {
                                folder = Constants.CacheDirectories.GIFS.folder
                            }
                            Constants.AttachmentType.GIF_NN.type -> {
                                folder = Constants.CacheDirectories.GIFS.folder
                            }
                        }

                        val path = File(context.cacheDir!!, folder)
                        if (!path.exists())
                            path.mkdirs()

                        val file = File(
                            path,
                            fileName
                        )

                        attachment.fileName = fileName
                        updateAttachment(attachment)

                        var inputStream: InputStream? = null
                        var outputStream: OutputStream? = null

                        try {

                            Timber.d("File Size=${body.contentLength()}")
                            val contentLength = body.contentLength()
                            inputStream = body.byteStream()
                            outputStream =
                                file.outputStream() /*encryptedFile.openFileOutput()*/
                            val data = ByteArray(4096)
                            var count: Int
                            var progress = 0
                            while (inputStream.read(data).also { count = it } != -1) {
                                yield()
                                outputStream.write(data, 0, count)
                                progress += count
                                val finalPercentage = (progress * 100 / contentLength)
                                if (finalPercentage > 0) {
                                    offer(
                                        DownloadAttachmentResult.Progress(
                                            itemPosition,
                                            finalPercentage.toFloat()
                                        )
                                    )
                                }

                                /*Timber.d(
                                    "Progress: $progress/${contentLength} >>>> $finalPercentage"
                                )*/
                            }
                            outputStream.flush()
                            Timber.d("File saved successfully!")

                            attachment.status =
                                Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status
                            updateAttachment(attachment)
                            if (BuildConfig.ENCRYPT_API && attachment.type != Constants.AttachmentType.GIF_NN.type) {
                                saveEncryptedFile(attachment)
                            }

                            offer(
                                DownloadAttachmentResult.Success(
                                    messageAndAttachmentRelation,
                                    itemPosition
                                )
                            )
                            close()
                        } catch (e: CancellationException) {
                            Timber.d("Job canceled")
                            attachment.status =
                                Constants.AttachmentStatus.DOWNLOAD_CANCEL.status
                            updateAttachment(attachment)
                            offer(
                                DownloadAttachmentResult.Cancel(
                                    messageAndAttachmentRelation, itemPosition
                                )
                            )
                            close()
                        } catch (e: IOException) {
                            Timber.e("IOException $e")
                            offer(
                                DownloadAttachmentResult.Error(
                                    attachment,
                                    "File not downloaded",
                                    itemPosition
                                )
                            )
                            close()
                            Timber.d("Failed to save the file!")
                        } finally {
                            inputStream?.close()
                            outputStream?.close()
                        }
                    }
                } else {
                    Timber.e("Response failure")
                    offer(
                        DownloadAttachmentResult.Error(
                            attachment,
                            "File not downloaded",
                            itemPosition
                        )
                    )
                    close()
                }
            } catch (e: Exception) {
                Timber.e("Last catch $e")
                attachment.status = Constants.AttachmentStatus.DOWNLOAD_CANCEL.status
                updateAttachment(attachment)
            }
        }
    }

    override fun updateAttachmentState(
        messageAndAttachmentRelation: MessageAttachmentRelation,
        state: Int
    ) {
        if (messageAndAttachmentRelation.attachmentEntityList.isNotEmpty()) {
            val firstAttachment = messageAndAttachmentRelation.attachmentEntityList.first()
            attachmentLocalDataSource.updateAttachmentState(firstAttachment.webId, state)
        }
    }

    override suspend fun copyFile(fileUri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver

            val cursor = contentResolver.query(
                fileUri,
                arrayOf(MediaStore.Files.FileColumns.MIME_TYPE),
                null,
                null,
                null
            )

            if (cursor != null && cursor.moveToFirst()) {
                cursor.use {
                    val mimeTypeIndex =
                        cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)
                    val mimeType = cursor.getStringOrNull(mimeTypeIndex)
                    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)

                    if (extension != null) {
                        val inputStream = context.contentResolver.openInputStream(fileUri)
                        if (inputStream != null) {
                            FileManager.copyFile(
                                context,
                                inputStream,
                                Constants.CacheDirectories.DOCUMENTOS.folder,
                                "${System.currentTimeMillis()}.$extension"
                            )
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    override fun verifyMessagesToDelete() {
        messageLocalDataSource.verifyMessagesToDelete()
    }

    override suspend fun setMessageRead(messageAndAttachmentRelation: MessageAttachmentRelation) {
        try {
            Timber.d("setMessageRead: ${messageAndAttachmentRelation.messageEntity.webId}")
            val response = napoleonApi.sendMessagesRead(
                MessagesReadReqDTO(
                    arrayListOf(messageAndAttachmentRelation.messageEntity.webId)
                )
            )

            if (response.isSuccessful) {
                Timber.d("Success: ${response.body()}")
                messageLocalDataSource.updateMessageStatus(
                    response.body()!!,
                    Constants.MessageStatus.READED.status
                )
            }
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }

    override suspend fun setMessageRead(messageId: Int, webId: String) {
        try {

            val messageAndAttachment = messageLocalDataSource.getMessageById(messageId, false)

            val webIdMessage = if (webId.isNotEmpty()) {
                webId
            } else {
                messageAndAttachment?.messageEntity?.webId
            }

            if ((messageAndAttachment?.messageEntity?.isMine == Constants.IsMine.NO.value) || webId.isNotEmpty()) {
                webIdMessage?.let {
                    val response = napoleonApi.sendMessagesRead(
                        MessagesReadReqDTO(
                            arrayListOf(webIdMessage)
                        )
                    )

                    if (response.isSuccessful) {
                        Timber.d("Success: ${response.body()}")
                        messageLocalDataSource.updateMessageStatus(
                            response.body()!!,
                            Constants.MessageStatus.READED.status
                        )
                    }
                }
            }
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }

    override suspend fun compressVideo(
        attachmentEntity: AttachmentEntity,
        srcFile: File,
        destFile: File,
        job: ProducerScope<*>
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

    override fun getFreeTrial(): Long {
        return sharedPreferencesManager.getLong(
            Constants.SharedPreferences.PREF_FREE_TRIAL
        )
    }

    override fun getMessageNotSent(contactId: Int): MessageNotSentEntity {
        return messageNotSentLocalDataSource.getMessageNotSetByContact(contactId)
    }

    override fun insertMessageNotSent(message: String, contactId: Int) {
        if (message.isEmpty()) {
            messageNotSentLocalDataSource.deleteMessageNotSentByContact(contactId)
        } else {
            messageNotSentLocalDataSource.insertMessageNotSent(
                MessageNotSentEntity(
                    id = 0,
                    message = message,
                    contactId = contactId
                )
            )
        }
    }

    override fun deleteMessageNotSent(contactId: Int) {
        messageNotSentLocalDataSource.deleteMessageNotSentByContact(contactId)
    }
}