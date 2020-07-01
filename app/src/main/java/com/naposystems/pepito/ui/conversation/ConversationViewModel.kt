package com.naposystems.pepito.ui.conversation

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.lifecycle.*
import com.naposystems.pepito.BuildConfig
import com.naposystems.pepito.R
import com.naposystems.pepito.crypto.message.CryptoMessage
import com.naposystems.pepito.dto.conversation.deleteMessages.DeleteMessagesReqDTO
import com.naposystems.pepito.dto.conversation.message.MessageReqDTO
import com.naposystems.pepito.dto.conversation.message.MessageResDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.entity.message.attachments.MediaStoreAudio
import com.naposystems.pepito.utility.*
import com.naposystems.pepito.utility.Utils.Companion.setupNotificationSound
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ConversationViewModel @Inject constructor(
    private val context: Context,
    private val repository: IContractConversation.Repository
) : ViewModel(), IContractConversation.ViewModel {

    private val cryptoMessage = CryptoMessage(context)

    private lateinit var user: User
    private lateinit var contact: Contact
    private var isVideoCall: Boolean = false
    lateinit var contactProfile: LiveData<Contact>

    private val _webServiceError = MutableLiveData<List<String>>()
    val webServiceError: LiveData<List<String>>
        get() = _webServiceError

    private lateinit var _messageMessages: LiveData<List<MessageAndAttachment>>
    val messageMessages: LiveData<List<MessageAndAttachment>>
        get() = _messageMessages

    private lateinit var _messagesSelected: LiveData<List<MessageAndAttachment>>
    val messagesSelected: LiveData<List<MessageAndAttachment>>
        get() = _messagesSelected

    private val _stringsCopy = MutableLiveData<List<String>>()
    val stringsCopy: LiveData<List<String>>
        get() = _stringsCopy

    private val _responseDeleteLocalMessages = MutableLiveData<Boolean>()
    val responseDeleteLocalMessages: LiveData<Boolean>
        get() = _responseDeleteLocalMessages

    private val _deleteMessagesForAllWsError = MutableLiveData<List<String>>()
    val deleteMessagesForAllWsError: LiveData<List<String>>
        get() = _deleteMessagesForAllWsError

    private val _contactCalledSuccessfully = MutableLiveData<String>()
    val contactCalledSuccessfully: LiveData<String>
        get() = _contactCalledSuccessfully

    private val _downloadProgress = MutableLiveData<DownloadAttachmentResult>()
    val downloadAttachmentProgress: LiveData<DownloadAttachmentResult>
        get() = _downloadProgress

    private val _uploadProgress = MutableLiveData<UploadResult>()
    val uploadProgress: LiveData<UploadResult>
        get() = _uploadProgress

    private val _documentCopied = MutableLiveData<File>()
    val documentCopied: LiveData<File>
        get() = _documentCopied

    private var countOldMessages: Int = 0

    init {
        _responseDeleteLocalMessages.value = false
        _webServiceError.value = ArrayList()
        _stringsCopy.value = emptyList()
    }

    private suspend fun copyAudioToAppFolder(fileDescriptor: ParcelFileDescriptor): File {

        val fileInputStream = FileInputStream(fileDescriptor.fileDescriptor)

        return FileManager.copyFile(
            context,
            fileInputStream,
            Constants.NapoleonCacheDirectories.AUDIOS.folder,
            "${System.currentTimeMillis()}.mp3"
        )
    }

    private fun setStatusErrorMessageAndAttachment(message: Message, attachment: Attachment?) {
        message.status = Constants.MessageStatus.ERROR.status
        repository.updateMessage(message)
        attachment?.let {
            attachment.status = Constants.AttachmentStatus.ERROR.status
            repository.updateAttachment(attachment)
        }
    }

    //region Implementation IContractConversation.ViewModel

    override fun getUser() = user

    override fun setContact(contact: Contact) {
        this.contact = contact
    }

    override fun getLocalMessages() {
        viewModelScope.launch {
            user = repository.getLocalUser()
            repository.verifyMessagesToDelete()
            _messageMessages = repository.getLocalMessages(contact.id)
        }
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    override fun saveMessageLocally(body: String, selfDestructTime: Int, quote: String) {
        saveMessageAndAttachment(body, null, 0, selfDestructTime, quote)
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    override fun saveMessageAndAttachment(
        messageString: String,
        attachment: Attachment?,
        numberAttachments: Int,
        selfDestructTime: Int,
        quote: String
    ) {
        viewModelScope.launch {
            val message = Message(
                id = 0,
                webId = "",
                body = messageString,
                quoted = quote,
                contactId = contact.id,
                updatedAt = 0,
                createdAt = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toInt(),
                isMine = Constants.IsMine.YES.value,
                status = Constants.MessageStatus.SENDING.status,
                numberAttachments = numberAttachments,
                messageType = Constants.MessageType.MESSAGE.type,
                selfDestructionAt = selfDestructTime
            )

            if (BuildConfig.ENCRYPT_API) {
                message.encryptBody(cryptoMessage)
            }

            val messageId = repository.insertMessage(message).toInt()
            Timber.d("insertMessage")

            message.id = messageId

            attachment?.let {
                attachment.messageId = messageId

                val attachmentId = repository.insertAttachment(attachment)
                attachment.id = attachmentId.toInt()
            }

            if (message.quoted.isNotEmpty()) {
                repository.insertQuote(quote, message)
            }

            sendMessageAndAttachment(
                attachment = attachment,
                message = message,
                numberAttachments = numberAttachments,
                selfDestructTime = selfDestructTime,
                quote = quote
            )
        }
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    override fun saveMessageWithAudioAttachment(
        mediaStoreAudio: MediaStoreAudio,
        selfDestructTime: Int,
        quote: String
    ) {
        viewModelScope.launch {
            val fileDescriptor = context.contentResolver
                .openFileDescriptor(mediaStoreAudio.contentUri, "r")

            if (fileDescriptor != null) {

                val audioFile = copyAudioToAppFolder(fileDescriptor)

                val attachment = Attachment(
                    id = 0,
                    messageId = 0,
                    webId = "",
                    messageWebId = "",
                    type = Constants.AttachmentType.AUDIO.type,
                    body = "",
                    uri = audioFile.name,
                    origin = Constants.AttachmentOrigin.AUDIO_SELECTION.origin,
                    thumbnailUri = "",
                    status = Constants.AttachmentStatus.SENDING.status,
                    extension = "mp3",
                    duration = mediaStoreAudio.duration
                )

                saveMessageAndAttachment(
                    messageString = "",
                    attachment = attachment,
                    numberAttachments = 1,
                    selfDestructTime = selfDestructTime,
                    quote = quote
                )
            }
        }
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    private suspend fun sendMessageAndAttachment(
        attachment: Attachment?,
        message: Message,
        numberAttachments: Int,
        selfDestructTime: Int,
        quote: String = ""
    ) {
        try {

            val messageReqDTO = MessageReqDTO(
                userDestination = contact.id,
                quoted = quote,
                body = message.getBody(cryptoMessage),
                numberAttachments = numberAttachments,
                destroy = selfDestructTime,
                messageType = Constants.MessageType.MESSAGE.type
            )

            val messageResponse = repository.sendMessage(messageReqDTO)

            if (messageResponse.isSuccessful) {
                val messageEntity = MessageResDTO.toMessageEntity(
                    message,
                    messageResponse.body()!!,
                    Constants.IsMine.YES.value
                )

                if (attachment != null) {
                    attachment.messageWebId = messageResponse.body()!!.id
                    uploadAttachment(attachment, messageEntity, selfDestructTime)
                } else {
                    messageEntity.status =
                        if (messageEntity.isMine == Constants.IsMine.NO.value) Constants.MessageStatus.UNREAD.status
                        else Constants.MessageStatus.SENT.status
                    repository.updateMessage(messageEntity)
                    Timber.d("updateMessage")
                }

                setupNotificationSound(context, R.raw.sound_message_sent)

            } else {
                setStatusErrorMessageAndAttachment(message, attachment)

                when (messageResponse.code()) {
                    422 -> _webServiceError.value =
                        repository.get422ErrorMessage(messageResponse)
                    else -> _webServiceError.value = repository.getErrorMessage(messageResponse)
                }
            }
        } catch (e: Exception) {
            setStatusErrorMessageAndAttachment(message, attachment)
            Timber.e(e)
        }
    }

    override fun updateStateSelectionMessage(contactId: Int, idMessage: Int, isSelected: Boolean) {
        viewModelScope.launch {
            repository.updateStateSelectionMessage(
                contactId,
                idMessage,
                Utils.convertBooleanToInvertedInt(isSelected)
            )
        }
    }

    override fun cleanSelectionMessages(contactId: Int) {
        viewModelScope.launch {
            repository.cleanSelectionMessages(contactId)
        }
    }

    override fun deleteMessagesSelected(contactId: Int, listMessages: List<MessageAndAttachment>) {
        viewModelScope.launch {
            repository.deleteMessagesSelected(contactId, listMessages)
            _responseDeleteLocalMessages.value = true
        }
    }

    override fun deleteMessagesForAll(contactId: Int, listMessages: List<MessageAndAttachment>) {
        viewModelScope.launch {
            try {

                val response =
                    repository.deleteMessagesForAll(
                        buildObjectDeleteMessages(
                            contactId,
                            listMessages
                        )
                    )

                if (response.isSuccessful) {
                    repository.deleteMessagesSelected(contactId, listMessages)
                    _responseDeleteLocalMessages.value = true
                } else {
                    when (response.code()) {
                        422 -> {
                            _deleteMessagesForAllWsError.value =
                                repository.get422ErrorDeleteMessagesForAll(response.errorBody()!!)
                        }
                        else -> {
                            _deleteMessagesForAllWsError.value =
                                repository.getErrorDeleteMessagesForAll(response.errorBody()!!)
                        }
                    }
                }
            } catch (ex: Exception) {
                Timber.d(ex)
                val error = context.getString(R.string.text_fail)
                _webServiceError.value = arrayListOf(error)
            }

        }
    }

    override fun deleteMessagesByStatusForMe(contactId: Int, status: Int) {
        viewModelScope.launch {
            repository.deleteMessagesByStatusForMe(contactId, status)
        }
    }

    override fun deleteMessagesByStatusForAll(contactId: Int, status: Int) {
        viewModelScope.launch {
            val messagesUnread = repository.getLocalMessagesByStatus(contactId, status)
            if (messagesUnread.count() > 0) {
                deleteMessagesForAll(contactId, messagesUnread)
            }
        }
    }

    override fun copyMessagesSelected(contactId: Int) {
        viewModelScope.launch {
            _stringsCopy.value = repository.copyMessagesSelected(contactId)
        }
    }

    override fun resetListStringCopy() {
        _stringsCopy.value = emptyList()
    }

    override fun setCountOldMessages(count: Int) {
        countOldMessages = count
    }

    override fun getCountOldMessages(): Int {
        return countOldMessages
    }

    override fun getMessagesSelected(contactId: Int) {
        viewModelScope.launch {
            _messagesSelected = repository.getMessagesSelected(contactId)
        }
    }

    override fun parsingListByTextBlock(listBody: List<String>): String {
        var stringOfReturn = String()
        listBody.forEachIndexed { index, body ->
            stringOfReturn += if (index < listBody.count() - 1)
                "$body\n"
            else
                body
        }
        return stringOfReturn
    }

    override fun sendTextMessagesRead() {
        viewModelScope.launch {
            repository.sendTextMessagesRead(contact.id)
            repository.sendMissedCallRead(contact.id)
        }
    }

    private fun buildObjectDeleteMessages(
        contactId: Int,
        listMessages: List<MessageAndAttachment>
    ): DeleteMessagesReqDTO {
        val listReturn = arrayListOf<String>()
        listMessages.forEach {
            listReturn.add(it.message.webId)
        }
        return DeleteMessagesReqDTO(
            userReceiver = contactId,
            messagesId = listReturn
        )
    }

    override fun getMessagePosition(messageAndAttachment: MessageAndAttachment): Int {
        var index = -1

        messageAndAttachment.quote?.let { quote ->
            messageMessages.value?.let { messagesList ->
                index = messagesList.indexOfFirst { it.message.id == quote.messageParentId }
            }
        }

        return index
    }

    override fun callContact() {
        viewModelScope.launch {
            val channel = "private-private.${contact.id}.${user.id}"
            try {
                repository.subscribeToCallChannel(channel)
                val response = repository.callContact(contact, isVideoCall)

                if (response.isSuccessful) {
                    response.body()?.let { _ ->
                        _contactCalledSuccessfully.value = channel
                    }
                } else {
                    repository.unSubscribeToChannel(contact, channel)
                }
            } catch (e: Exception) {
                repository.unSubscribeToChannel(contact, channel)
                Timber.e(e)
            }
        }
    }

    override fun resetContactCalledSuccessfully() {
        _contactCalledSuccessfully.value = null
    }

    override fun setIsVideoCall(isVideoCall: Boolean) {
        this.isVideoCall = isVideoCall
    }

    override fun isVideoCall() = this.isVideoCall

    override fun resetIsVideoCall() {
        this.isVideoCall = false
    }

    override fun uploadAttachment(
        attachment: Attachment,
        message: Message,
        selfDestructTime: Int
    ) {
        viewModelScope.launch {
            message.selfDestructionAt = selfDestructTime
            try {
                if (message.status == Constants.MessageStatus.ERROR.status && message.webId.isEmpty()) {
                    val messageReqDTO = MessageReqDTO(
                        userDestination = contact.id,
                        quoted = message.quoted,
                        body = message.getBody(cryptoMessage),
                        numberAttachments = 1,
                        destroy = message.selfDestructionAt,
                        messageType = Constants.MessageType.MESSAGE.type
                    )

                    val messageResponse = repository.sendMessage(messageReqDTO)

                    if (messageResponse.isSuccessful) {
                        val messageEntity = MessageResDTO.toMessageEntity(
                            message,
                            messageResponse.body()!!,
                            Constants.IsMine.YES.value
                        )

                        attachment.messageWebId = messageResponse.body()!!.id
                        uploadAttachment(attachment, messageEntity, selfDestructTime)
                    } else {
                        setStatusErrorMessageAndAttachment(message, attachment)

                        when (messageResponse.code()) {
                            422 -> _webServiceError.value =
                                repository.get422ErrorMessage(messageResponse)
                            else -> _webServiceError.value =
                                repository.getErrorMessage(messageResponse)
                        }
                    }
                } else {
                    repository.suspendUpdateAttachment(attachment)
                    repository.uploadAttachment(attachment, message)
                        .flowOn(Dispatchers.IO)
                        .collect {
                            _uploadProgress.value = it
                        }
                }
            } catch (e: Exception) {
                setStatusErrorMessageAndAttachment(message, attachment)
                Timber.e(e)
            }
        }
    }

    override fun downloadAttachment(messageAndAttachment: MessageAndAttachment, itemPosition: Int) {
        viewModelScope.launch {
            repository.downloadAttachment(messageAndAttachment, itemPosition)
                .flowOn(Dispatchers.IO)
                .catch {

                    Timber.e("catch flow")

                    val message = messageAndAttachment.message
                    val firstAttachment = messageAndAttachment.getFirstAttachment()

                    message.status = Constants.MessageStatus.ERROR.status
                    updateMessage(message)

                    if (firstAttachment != null) {
                        firstAttachment.status = Constants.AttachmentStatus.DOWNLOAD_CANCEL.status
                        updateAttachment(firstAttachment)
                    }

                    _downloadProgress.value =
                        DownloadAttachmentResult.Cancel(messageAndAttachment, itemPosition)
                }
                .onCompletion {
                    _downloadProgress.value =
                        DownloadAttachmentResult.Success(messageAndAttachment, itemPosition)
                }
                .collect {
                    _downloadProgress.value = it
                }
        }
    }

    override fun updateMessage(message: Message) {
        repository.updateMessage(message)
    }

    override fun updateAttachment(attachment: Attachment) {
        repository.updateAttachment(attachment)
    }

    override fun sendDocumentAttachment(fileUri: Uri) {
        viewModelScope.launch {
            val file = repository.copyFile(fileUri)

            if (file != null) {
                _documentCopied.value = file
            }
        }
    }

    override fun resetDocumentCopied() {
        _documentCopied.value = null
    }

    override fun resetUploadProgress() {
        _uploadProgress.value = null
    }

    override fun sendMessageRead(messageAndAttachment: MessageAndAttachment) {
        viewModelScope.launch {
            repository.setMessageRead(messageAndAttachment)
        }
    }

    override fun sendMessageRead(messageWebId: String) {
        viewModelScope.launch {
            repository.setMessageRead(messageWebId)
        }
    }

    override fun reSendMessage(message: Message, selfDestructTime: Int) {
        viewModelScope.launch {
            try {

                val messageReqDTO = MessageReqDTO(
                    userDestination = contact.id,
                    quoted = message.quoted,
                    body = message.getBody(cryptoMessage),
                    numberAttachments = 0,
                    destroy = selfDestructTime,
                    messageType = Constants.MessageType.MESSAGE.type
                )

                val messageResponse = repository.sendMessage(messageReqDTO)

                if (messageResponse.isSuccessful) {
                    val messageEntity = MessageResDTO.toMessageEntity(
                        message,
                        messageResponse.body()!!,
                        Constants.IsMine.YES.value
                    )

                    messageEntity.status =
                        if (messageEntity.isMine == Constants.IsMine.NO.value) Constants.MessageStatus.UNREAD.status
                        else Constants.MessageStatus.SENT.status
                    repository.updateMessage(messageEntity)
                    Timber.d("updateMessage")
                } else {
                    setStatusErrorMessageAndAttachment(message, null)

                    when (messageResponse.code()) {
                        422 -> _webServiceError.value =
                            repository.get422ErrorMessage(messageResponse)
                        else -> _webServiceError.value = repository.getErrorMessage(messageResponse)
                    }
                }
            } catch (e: Exception) {
                setStatusErrorMessageAndAttachment(message, null)
                Timber.e(e)
            }
        }
    }

    //endregion
}
