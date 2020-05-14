package com.naposystems.pepito.repository.conversation

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.database.getStringOrNull
import androidx.lifecycle.LiveData
import com.naposystems.pepito.db.dao.attachment.AttachmentDataSource
import com.naposystems.pepito.db.dao.message.MessageDataSource
import com.naposystems.pepito.db.dao.quoteMessage.QuoteDataSource
import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.dto.conversation.call.CallContactReqDTO
import com.naposystems.pepito.dto.conversation.call.CallContactResDTO
import com.naposystems.pepito.dto.conversation.deleteMessages.DeleteMessage422DTO
import com.naposystems.pepito.dto.conversation.deleteMessages.DeleteMessagesErrorDTO
import com.naposystems.pepito.dto.conversation.deleteMessages.DeleteMessagesReqDTO
import com.naposystems.pepito.dto.conversation.deleteMessages.DeleteMessagesResDTO
import com.naposystems.pepito.dto.conversation.message.*
import com.naposystems.pepito.dto.conversation.socket.AuthReqDTO
import com.naposystems.pepito.dto.conversation.socket.HeadersReqDTO
import com.naposystems.pepito.dto.conversation.socket.SocketReqDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.entity.message.Quote
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.ui.conversation.IContractConversation
import com.naposystems.pepito.utility.*
import com.naposystems.pepito.webService.NapoleonApi
import com.naposystems.pepito.webService.ProgressRequestBody
import com.naposystems.pepito.webService.socket.IContractSocketService
import com.squareup.moshi.Moshi
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.flow.*
import okhttp3.*
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

    override suspend fun subscribeToChannel(userToChat: Contact): String {

        var user: User? = null

        coroutineScope {
            user = getLocalUser()
        }

        val headersReqDTO = HeadersReqDTO(
            firebaseId
        )

        val authReqDTO = AuthReqDTO(
            headersReqDTO
        )

        var minorId: String = userToChat.id.toString()
        var mayorId: String = user!!.id.toString()

        if (user!!.id < userToChat.id) {
            mayorId = userToChat.id.toString()
            minorId = user!!.id.toString()
        }

        val channelName =
            "private-private.${minorId}.${mayorId}"

        val socketReqDTO = SocketReqDTO(
            channelName,
            authReqDTO
        )

        socketService.subscribe(SocketReqDTO.toJSONObject(socketReqDTO))

        return channelName
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

        socketService.unSubscribe(SocketReqDTO.toJSONObject(socketReqDTO), channelName)
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
            withContext(Dispatchers.IO) {
                val requestBodyMessageId = createPartFromString(attachment.messageWebId)
                val requestBodyType = createPartFromString(attachment.type)

                val job = launch(Dispatchers.IO) {
                    val requestBodyFilePart =
                        createPartFromFile(attachment, this, object : ProgressRequestBody.Listener {
                            override fun onRequestProgress(
                                bytesWritten: Int,
                                contentLength: Long,
                                progress: Int
                            ) {
                                offer(UploadResult.Progress(attachment, progress.toLong()))
                            }

                            override fun onRequestCancel() {
                                try {
                                    if (!isClosedForSend) {
                                        offer(UploadResult.Cancel(attachment, message))
                                    } else {
                                        message.status = Constants.MessageStatus.SENDING.status
                                        updateMessage(message)
                                        attachment.status =
                                            Constants.AttachmentStatus.UPLOAD_CANCEL.status
                                        updateAttachment(attachment)
                                    }
                                } catch (e: ClosedSendChannelException) {
                                    message.status = Constants.MessageStatus.SENDING.status
                                    updateMessage(message)
                                    attachment.status =
                                        Constants.AttachmentStatus.UPLOAD_CANCEL.status
                                    updateAttachment(attachment)
                                }
                            }
                        })

                    val response = napoleonApi.sendMessageAttachment(
                        messageId = requestBodyMessageId,
                        attachmentType = requestBodyType,
                        file = requestBodyFilePart
                    )

                    if (response.isSuccessful) {
                        Timber.d("Envió el puto archivo")

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
                        offer(UploadResult.Success(attachment))
                    } else {
                        setStatusErrorMessageAndAttachment(message, attachment)
                        offer(UploadResult.Error(attachment, "Algo ha salido mal", null))
                    }
                }
                offer(UploadResult.Start(attachment, job))
            }
        } catch (e: FileNotFoundException) {
            Timber.e(e)
            offer(UploadResult.Cancel(attachment, message))
        } catch (e: Exception) {
            Timber.e(e)
            offer(UploadResult.Cancel(attachment, message))
        }
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
        attachment: Attachment,
        job: CoroutineScope,
        listener: ProgressRequestBody.Listener
    ): MultipartBody.Part {

        val subfolder =
            FileManager.getSubfolderByAttachmentType(attachmentType = attachment.type)

        val fileUri = Utils.getFileUri(
            context = context, fileName = attachment.uri, subFolder = subfolder
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
                job,
                byteArray,
                mediaType!!,
                listener
            )

        Timber.d("before return MultiparBody")
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

    override suspend fun sendMessagesRead(contactId: Int) {
        val messagesUnread =
            messageLocalDataSource.getMessagesByStatus(
                contactId,
                Constants.MessageStatus.UNREAD.status
            )

        if (messagesUnread.isNotEmpty()) {
            try {
                val response = napoleonApi.sendMessagesRead(
                    MessagesReadReqDTO(
                        messagesUnread
                    )
                )

                if (response.isSuccessful) {
                    messageLocalDataSource.updateMessageStatus(
                        response.body()!!,
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
        attachmentLocalDataSource.updateAttachment(attachment)
    }

    override suspend fun suspendUpdateAttachment(attachment: Attachment) {
        attachmentLocalDataSource.suspendUpdateAttachment(attachment)
    }

    override fun insertQuote(quoteWebId: String, message: Message) {

        val originalMessage =
            messageLocalDataSource.getMessageByWebId(quoteWebId)

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
                thumbnailUri = firstAttachment?.uri ?: "",
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

        socketService.subscribeToCallChannel(
            channel,
            SocketReqDTO.toJSONObject(socketReqDTO)
        )
    }

    override suspend fun downloadAttachment(
        attachment: Attachment,
        itemPosition: Int
    ): Flow<DownloadAttachmentResult> = channelFlow {
        withContext(Dispatchers.IO) {
            val job = launch(Dispatchers.IO) {
                try {
                    val response = napoleonApi.downloadFileByUrl(attachment.body)

                    if (response.isSuccessful) {
                        response.body()?.let { body ->
                            try {
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

                                val fileName = "${attachment.webId}.${attachment.extension}"

                                val file = File(
                                    path,
                                    fileName
                                )

                                attachment.uri = fileName
                                updateAttachment(attachment)

                                var inputStream: InputStream? = null
                                var outputStream: OutputStream? = null

                                try {

                                    Timber.d("File Size=${body.contentLength()}")
                                    val contentLength = body.contentLength()
                                    inputStream = body.byteStream()
                                    outputStream =
                                        file.outputStream() /*encryptedFile.openFileOutput()*/
                                    val data = ByteArray(contentLength.toInt())
                                    var count: Int
                                    var progress = 0
                                    while (inputStream.read(data).also { count = it } != -1) {
                                        yield()
                                        outputStream.write(data, 0, count)
                                        progress += count
                                        val finalPercentage = (progress * 100 / contentLength)
                                        offer(
                                            DownloadAttachmentResult.Progress(
                                                itemPosition,
                                                finalPercentage
                                            )
                                        )
                                        Timber.d(
                                            "Progress: $progress/${contentLength} >>>> $finalPercentage"
                                        )
                                    }
                                    outputStream.flush()
                                    Timber.d("File saved successfully!")
                                    offer(
                                        DownloadAttachmentResult.Success(
                                            attachment,
                                            itemPosition
                                        )
                                    )
                                } catch (e: CancellationException) {
                                    Timber.d("Job canceled")
                                    attachment.status =
                                        Constants.AttachmentStatus.DOWNLOAD_CANCEL.status
                                    updateAttachment(attachment)
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                    offer(
                                        DownloadAttachmentResult.Error(
                                            attachment,
                                            "File not downloaded"
                                        )
                                    )
                                    Timber.d("Failed to save the file!")
                                } finally {
                                    inputStream?.close()
                                    outputStream?.close()
                                }
                            } catch (e: IOException) {
                                offer(
                                    DownloadAttachmentResult.Error(
                                        attachment,
                                        "File not downloaded"
                                    )
                                )
                                Timber.e(e)
                            }
                        }
                    } else {
                        offer(
                            DownloadAttachmentResult.Error(
                                attachment,
                                "File not downloaded"
                            )
                        )
                    }
                } catch (e: Exception) {
                    offer(
                        DownloadAttachmentResult.Error(
                            attachment,
                            "File not downloaded"
                        )
                    )
                    Timber.e(e)
                }
            }
            offer(DownloadAttachmentResult.Start(itemPosition, job))
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
}