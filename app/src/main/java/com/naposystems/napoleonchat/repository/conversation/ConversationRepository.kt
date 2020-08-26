package com.naposystems.napoleonchat.repository.conversation

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.database.getStringOrNull
import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.db.dao.attachment.AttachmentDataSource
import com.naposystems.napoleonchat.db.dao.message.MessageDataSource
import com.naposystems.napoleonchat.db.dao.quoteMessage.QuoteDataSource
import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.dto.conversation.call.CallContactReqDTO
import com.naposystems.napoleonchat.dto.conversation.call.CallContactResDTO
import com.naposystems.napoleonchat.dto.conversation.deleteMessages.DeleteMessage422DTO
import com.naposystems.napoleonchat.dto.conversation.deleteMessages.DeleteMessagesErrorDTO
import com.naposystems.napoleonchat.dto.conversation.deleteMessages.DeleteMessagesReqDTO
import com.naposystems.napoleonchat.dto.conversation.deleteMessages.DeleteMessagesResDTO
import com.naposystems.napoleonchat.dto.conversation.message.*
import com.naposystems.napoleonchat.dto.conversation.socket.AuthReqDTO
import com.naposystems.napoleonchat.dto.conversation.socket.HeadersReqDTO
import com.naposystems.napoleonchat.dto.conversation.socket.SocketReqDTO
import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.entity.User
import com.naposystems.napoleonchat.entity.message.Message
import com.naposystems.napoleonchat.entity.message.MessageAndAttachment
import com.naposystems.napoleonchat.entity.message.Quote
import com.naposystems.napoleonchat.entity.message.attachments.Attachment
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.ui.conversation.IContractConversation
import com.naposystems.napoleonchat.utility.*
import com.naposystems.napoleonchat.webService.NapoleonApi
import com.naposystems.napoleonchat.webService.ProgressRequestBody
import com.naposystems.napoleonchat.webService.socket.IContractSocketService
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
    private val socketService: IContractSocketService.SocketService,
    private val userLocalDataSource: UserLocalDataSource,
    private val messageLocalDataSource: MessageDataSource,
    private val attachmentLocalDataSource: AttachmentDataSource,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val napoleonApi: NapoleonApi,
    private val quoteDataSource: QuoteDataSource
) :
    IContractConversation.Repository {

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    private val firebaseId: String by lazy {
        sharedPreferencesManager
            .getString(Constants.SharedPreferences.PREF_FIREBASE_ID, "")
    }

    override fun unSubscribeToChannel(userToChat: Contact, channelName: String) {
        val headersReqDTO = HeadersReqDTO(
            firebaseId
        )

        val authReqDTO = AuthReqDTO(
            headersReqDTO
        )

        val socketReqDTO = SocketReqDTO(
            channelName,
            authReqDTO
        )

        socketService.unSubscribeCallChannel(channelName)
    }

    override fun getLocalMessages(contactId: Int): LiveData<List<MessageAndAttachment>> {
        return messageLocalDataSource.getMessages(contactId)
    }

    override suspend fun getQuoteId(quoteWebId: String): Int {
        return messageLocalDataSource.getQuoteId(quoteWebId)
    }

    override fun getLocalMessagesByStatus(contactId: Int, status: Int): List<MessageAndAttachment> {
        return messageLocalDataSource.getLocalMessagesByStatus(contactId, status)
    }

    override suspend fun sendMessage(messageReqDTO: MessageReqDTO): Response<MessageResDTO> {
        return napoleonApi.sendMessage(messageReqDTO)
    }

    override suspend fun uploadAttachment(
        attachment: Attachment,
        message: Message
    ) = channelFlow<UploadResult> {
        try {
            updateAttachment(attachment)
            message.status = Constants.MessageStatus.SENDING.status
            updateMessage(message)
            offer(UploadResult.Start(attachment, this))

            val path =
                File(context.cacheDir!!, FileManager.getSubfolderByAttachmentType(attachment.type))
            if (!path.exists())
                path.mkdirs()
            val sourceFile = File(path, attachment.fileName)
            val destFile =
                File(
                    path, "${attachment.fileName
                        .replace("_compress", "")
                        .split('.')[0]}_compress.${attachment.extension}"
                )

            compressVideo(attachment, sourceFile, destFile, this)
                .collect {
                    when (it) {
                        is VideoCompressResult.Start -> {
                            Timber.d("tmessages VideoCompressResult.Start")
                        }
                        is VideoCompressResult.Success -> {
                            Timber.d("tmessages VideoCompressResult.Success")
                            if (it.srcFile.isFile && it.srcFile.exists() && !attachment.isCompressed && attachment.type == Constants.AttachmentType.VIDEO.type)
                                it.srcFile.delete()
                            attachment.fileName =
                                if (attachment.type == Constants.AttachmentType.VIDEO.type) it.destFile.name else it.srcFile.name
                            attachment.isCompressed = true
                            updateAttachment(attachment)

                            val requestBodyMessageId = createPartFromString(attachment.messageWebId)
                            val requestBodyType = createPartFromString(attachment.type)
                            val requestBodyDuration =
                                createPartFromString(attachment.duration.toString())

                            val requestBodyFilePart =
                                createPartFromFile(
                                    this@channelFlow,
                                    attachment,
                                    this as Job
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
                                offer(UploadResult.Success(attachment))
                            } else {
                                setStatusErrorMessageAndAttachment(message, attachment)
                                offer(UploadResult.Error(attachment, "Algo ha salido mal", null))
                            }
                        }
                        is VideoCompressResult.Progress -> {
                            Timber.d("tmessages VideoCompressResult.Progress ${it.progress}")
                            offer(
                                UploadResult.CompressProgress(
                                    attachment,
                                    it.progress.toLong(),
                                    this
                                )
                            )
                        }
                        is VideoCompressResult.Fail -> {
                            setStatusErrorMessageAndAttachment(message, attachment)
                            offer(UploadResult.Error(attachment, "Algo ha salido mal", null))
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

    private fun createPartFromString(string: String): RequestBody {
        return RequestBody.create(MultipartBody.FORM, string)
    }

    private fun createPartFromFile(
        channel: ProducerScope<UploadResult>,
        attachment: Attachment,
        job: Job
    ): MultipartBody.Part {

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
                attachment,
                channel,
                byteArray,
                mediaType!!
            )

        Timber.d("before return MultiparBody, $job")
        return MultipartBody.Part.createFormData(
            "body",
            "${System.currentTimeMillis()}.$extension",
            progressRequestBody
        )
    }

    override suspend fun getLocalUser(): User {
        return userLocalDataSource.getUser(firebaseId)
    }

    override fun insertMessage(message: Message): Long {
        return messageLocalDataSource.insertMessage(message)
    }

    override fun insertListMessage(messageList: List<Message>) {
        messageLocalDataSource.insertListMessage(messageList)
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

    override suspend fun sendTextMessagesRead(contactId: Int) {
        val messagesUnread =
            messageLocalDataSource.getTextMessagesByStatus(
                contactId,
                Constants.MessageStatus.UNREAD.status
            )

        val textMessagesUnread = messagesUnread.filter { it.attachmentList.isEmpty() }
        val locationMessagesUnread =
            messagesUnread.filter { it.getFirstAttachment()?.type == Constants.AttachmentType.LOCATION.type }
        val textMessagesUnreadIds = textMessagesUnread.map { it.message.webId }
        val locationMessagesUnreadIds = locationMessagesUnread.map { it.message.webId }

        val listIds = mutableListOf<String>()
        listIds.addAll(textMessagesUnreadIds)
        listIds.addAll(locationMessagesUnreadIds)

        if (listIds.isNotEmpty()) {
            try {
                val response = napoleonApi.sendMessagesRead(
                    MessagesReadReqDTO(
                        listIds
                    )
                )

                if (response.isSuccessful) {
                    messageLocalDataSource.updateMessageStatus(
                        listIds,
                        Constants.MessageStatus.READED.status
                    )
                }
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    override suspend fun sendMissedCallRead(contactId: Int) {
        val messagesUnread =
            messageLocalDataSource.getMissedCallsByStatus(
                contactId,
                Constants.MessageStatus.UNREAD.status
            )

        val textMessagesUnread = messagesUnread.filter { it.attachmentList.isEmpty() }
        val textMessagesUnreadIds = textMessagesUnread.map { it.message.webId }

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

    override fun insertAttachment(attachment: Attachment): Long {
        return attachmentLocalDataSource.insertAttachment(attachment)
    }

    override fun insertAttachments(listAttachment: List<Attachment>): List<Long> {
        return attachmentLocalDataSource.insertAttachments(listAttachment)
    }

    override fun updateAttachment(attachment: Attachment) {
        Timber.d("update attachment")
        attachmentLocalDataSource.updateAttachment(attachment)
    }

    override suspend fun suspendUpdateAttachment(attachment: Attachment) {
        attachmentLocalDataSource.suspendUpdateAttachment(attachment)
    }

    override suspend fun insertQuote(quoteWebId: String, message: Message) {

        val originalMessage =
            messageLocalDataSource.getMessageByWebId(quoteWebId, false)

        if (originalMessage != null) {
            var firstAttachment: Attachment? = null

            if (originalMessage.attachmentList.isNotEmpty()) {
                firstAttachment = originalMessage.attachmentList.first()
            }

            val quote = Quote(
                id = 0,
                messageId = message.id,
                contactId = originalMessage.message.contactId,
                body = originalMessage.message.body,
                attachmentType = firstAttachment?.type ?: "",
                thumbnailUri = firstAttachment?.fileName ?: "",
                messageParentId = originalMessage.message.id,
                isMine = originalMessage.message.isMine
            )

            quoteDataSource.insertQuote(quote)
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
        listMessages: List<MessageAndAttachment>
    ) {
        listMessages.filter { messageAndAttachment ->
            messageAndAttachment.attachmentList.count() > 0 &&
            messageAndAttachment.attachmentList[0].type == Constants.AttachmentType.AUDIO.type
        }.let { listMessagesFiltered ->
            if (listMessagesFiltered.count() > 0) {
                RxBus.publish(RxEvent.MessagesToEliminate(listMessagesFiltered))
            }
        }
        messageLocalDataSource.deleteMessagesSelected(contactId, listMessages)
    }

    override suspend fun deleteMessagesForAll(deleteMessagesReqDTO: DeleteMessagesReqDTO): Response<DeleteMessagesResDTO> {
        return napoleonApi.deleteMessagesForAll(deleteMessagesReqDTO)
    }

    override suspend fun copyMessagesSelected(contactId: Int): List<String> {
        return messageLocalDataSource.copyMessagesSelected(contactId)
    }

    override suspend fun getMessagesSelected(contactId: Int): LiveData<List<MessageAndAttachment>> {
        return messageLocalDataSource.getMessagesSelected(contactId)
    }

    override suspend fun deleteMessagesByStatusForMe(contactId: Int, status: Int) {
        messageLocalDataSource.deleteMessagesByStatusForMe(contactId, status)
    }

    override fun get422ErrorMessage(response: Response<MessageResDTO>): ArrayList<String> {
        val adapter = moshi.adapter(Message422DTO::class.java)

        val conversationError = adapter.fromJson(response.errorBody()!!.string())

        return WebServiceUtils.get422Errors(conversationError!!)
    }

    override fun getErrorMessage(response: Response<MessageResDTO>): ArrayList<String> {

        val adapter = moshi.adapter(MessageErrorDTO::class.java)

        val conversationError = adapter.fromJson(response.errorBody()!!.string())

        val errorList = ArrayList<String>()

        errorList.add(conversationError!!.error)

        return errorList
    }

    override fun get422ErrorDeleteMessagesForAll(response: ResponseBody): ArrayList<String> {
        val adapter = moshi.adapter(DeleteMessage422DTO::class.java)

        val conversationError = adapter.fromJson(response.string())

        return WebServiceUtils.get422Errors(conversationError!!)
    }

    override fun getErrorDeleteMessagesForAll(response: ResponseBody): ArrayList<String> {
        val adapter = moshi.adapter(DeleteMessagesErrorDTO::class.java)

        val conversationError = adapter.fromJson(response.string())

        val errorList = ArrayList<String>()

        errorList.add(conversationError!!.error)

        return errorList
    }

    override suspend fun callContact(
        contact: Contact,
        isVideoCall: Boolean
    ): Response<CallContactResDTO> {
        val callContactReqDTO = CallContactReqDTO(
            contactToCall = contact.id,
            isVideoCall = isVideoCall
        )

        return napoleonApi.callContact(callContactReqDTO)
    }

    override fun subscribeToCallChannel(channel: String) {

        val headersReqDTO = HeadersReqDTO(
            firebaseId
        )

        val authReqDTO = AuthReqDTO(
            headersReqDTO
        )

        val socketReqDTO = SocketReqDTO(
            channel,
            authReqDTO
        )

        socketService.subscribeToCallChannel(channel)
    }

    override suspend fun downloadAttachment(
        messageAndAttachment: MessageAndAttachment,
        itemPosition: Int
    ): Flow<DownloadAttachmentResult> = channelFlow<DownloadAttachmentResult> {
        messageAndAttachment.getFirstAttachment()?.let { attachment ->
            val fileName = "${System.currentTimeMillis()}.${attachment.extension}"
            /*val fileName = "${System.currentTimeMillis()}.${attachment.extension}"
            attachment.status = Constants.AttachmentStatus.DOWNLOADING.status
            attachment.uri = fileName
            Timber.d("Attachment status: ${attachment.status}, uri: ${attachment.uri}")
            updateAttachment(attachment)
            offer(DownloadAttachmentResult.Start(itemPosition, this))*/

            try {
                val response = napoleonApi.downloadFileByUrl(attachment.body)
//                val response = napoleonApi.downloadFileByUrl("https://video-lga3-1.xx.fbcdn.net/v/t39.24130-2/10000000_252314952661745_7854699925195670435_n.mp4?_nc_cat=101&_nc_sid=985c63&efg=eyJ2ZW5jb2RlX3RhZyI6Im9lcF9oZCJ9&_nc_ohc=yZMEbG8J9AsAX91wp5i&_nc_ht=video-lga3-1.xx&oh=d84d0cad44e97099db93202661c50217&oe=5F0F53B3")

                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        var folder = ""

                        when (attachment.type) {
                            Constants.AttachmentType.IMAGE.type,
                            Constants.AttachmentType.LOCATION.type -> {
                                folder =
                                    Constants.NapoleonCacheDirectories.IMAGES.folder
                            }
                            Constants.AttachmentType.AUDIO.type -> {
                                folder =
                                    Constants.NapoleonCacheDirectories.AUDIOS.folder
                            }
                            Constants.AttachmentType.VIDEO.type -> {
                                folder =
                                    Constants.NapoleonCacheDirectories.VIDEOS.folder
                            }
                            Constants.AttachmentType.DOCUMENT.type -> {
                                folder =
                                    Constants.NapoleonCacheDirectories.DOCUMENTOS.folder
                            }
                            Constants.AttachmentType.GIF.type -> {
                                folder = Constants.NapoleonCacheDirectories.GIFS.folder
                            }
                            Constants.AttachmentType.GIF_NN.type -> {
                                folder = Constants.NapoleonCacheDirectories.GIFS.folder
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
                                            finalPercentage
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
                                    messageAndAttachment,
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
                                    messageAndAttachment, itemPosition
                                )
                            )
                            close()
                        } catch (e: IOException) {
                            Timber.e("IOException $e")
                            offer(
                                DownloadAttachmentResult.Error(
                                    attachment,
                                    "File not downloaded"
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
                            "File not downloaded"
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

    override fun updateAttachmentState(messageAndAttachment: MessageAndAttachment, state: Int) {
        if (messageAndAttachment.attachmentList.isNotEmpty()) {
            val firstAttachment = messageAndAttachment.attachmentList.first()
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
                                Constants.NapoleonCacheDirectories.DOCUMENTOS.folder,
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

    override suspend fun setMessageRead(messageAndAttachment: MessageAndAttachment) {
        try {
            Timber.d("setMessageRead: ${messageAndAttachment.message.webId}")
            val response = napoleonApi.sendMessagesRead(
                MessagesReadReqDTO(
                    arrayListOf(messageAndAttachment.message.webId)
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

    override suspend fun setMessageRead(messageWebId: String) {
        try {
            Timber.d("setMessageRead: $messageWebId")
            val messageAndAttachment = messageLocalDataSource.getMessageByWebId(messageWebId, false)

            if (messageAndAttachment?.message?.status == Constants.MessageStatus.UNREAD.status &&
                messageAndAttachment.message.isMine == Constants.IsMine.NO.value
            ) {
                val response = napoleonApi.sendMessagesRead(
                    MessagesReadReqDTO(
                        arrayListOf(messageWebId)
                    )
                )

                if (response.isSuccessful) {
                    Timber.d("Success: ${response.body()}")
                    messageLocalDataSource.updateMessageStatus(
                        response.body()!!,
                        Constants.MessageStatus.READED.status
                    )
                }
            } else if(messageAndAttachment == null) {
                val response = napoleonApi.sendMessagesRead(
                    MessagesReadReqDTO(
                        arrayListOf(messageWebId)
                    )
                )

                if (response.isSuccessful) {
                    Timber.d("Success: ${response.body()}")
                }
            }
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }

    override suspend fun reSendMessage(messageAndAttachment: MessageAndAttachment) {

    }

    override suspend fun compressVideo(
        attachment: Attachment,
        srcFile: File,
        destFile: File,
        job: ProducerScope<*>
    ) = flow<VideoCompressResult> {

        if (attachment.type == Constants.AttachmentType.VIDEO.type && !attachment.isCompressed) {
            if (destFile.exists())
                destFile.delete()

            VideoCompressK.compressVideoLow(
                srcFile, destFile, job
            ).collect {
                emit(it)
            }
        } else {
            emit(VideoCompressResult.Success(srcFile, destFile))
        }
    }
}