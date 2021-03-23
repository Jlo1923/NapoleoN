package com.naposystems.napoleonchat.ui.conversation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.source.remote.dto.conversation.deleteMessages.DeleteMessagesReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageResDTO
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.local.entity.MessageNotSentEntity
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.model.MediaStoreAudio
import com.naposystems.napoleonchat.service.uploadService.UploadService
import com.naposystems.napoleonchat.utility.*
import com.naposystems.napoleonchat.utility.Utils.Companion.compareDurationAttachmentWithSelfAutoDestructionInSeconds
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList

class ConversationViewModel @Inject constructor(
    private val cryptoMessage: CryptoMessage,
    private val context: Context,
    private val repository: IContractConversation.Repository
) : ViewModel(), IContractConversation.ViewModel {

    private lateinit var userEntity: UserEntity
    private lateinit var contact: ContactEntity
    private var isVideoCall: Boolean = false
    lateinit var contactProfile: LiveData<ContactEntity>

    private val _webServiceError = MutableLiveData<List<String>>()
    val webServiceError: LiveData<List<String>>
        get() = _webServiceError

    private lateinit var _messageMessagesRelation: LiveData<List<MessageAttachmentRelation>>
    val messageMessagesRelation: LiveData<List<MessageAttachmentRelation>>
        get() = _messageMessagesRelation

    private lateinit var _messagesSelected: LiveData<List<MessageAttachmentRelation>>
    val messagesSelected: LiveData<List<MessageAttachmentRelation>>
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

    private val _stateMessage = MutableLiveData<StateMessage>()
    val stateMessage: LiveData<StateMessage>
        get() = _stateMessage

    private val _documentCopied = MutableLiveData<File>()
    val documentCopied: LiveData<File>
        get() = _documentCopied

    private val _noInternetConnection = MutableLiveData<Boolean>()
    val noInternetConnection: LiveData<Boolean>
        get() = _noInternetConnection

    private val _newMessageSend = MutableLiveData<Boolean>()
    val newMessageSend: LiveData<Boolean>
        get() = _newMessageSend

    private val _messageNotSent = MutableLiveData<MessageNotSentEntity>()
    val messageNotSentEntity: LiveData<MessageNotSentEntity>
        get() = _messageNotSent

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
            Constants.CacheDirectories.AUDIOS.folder,
            "${System.currentTimeMillis()}.mp3"
        )
    }

    private fun setStatusErrorMessageAndAttachment(messageEntity: MessageEntity, attachmentEntity: AttachmentEntity?) {
        messageEntity.status = Constants.MessageStatus.ERROR.status
        repository.updateMessage(messageEntity)
        attachmentEntity?.let {
            attachmentEntity.status = Constants.AttachmentStatus.ERROR.status
            repository.updateAttachment(attachmentEntity)
        }
    }

    //region Implementation IContractConversation.ViewModel

    override fun getUser() = userEntity

    override fun setContact(contact: ContactEntity) {
        this.contact = contact
    }

    override fun getLocalMessages() {
        viewModelScope.launch {
            userEntity = repository.getLocalUser()
            repository.verifyMessagesToDelete()
            _messageMessagesRelation = repository.getLocalMessages(contact.id)
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
        attachmentEntity: AttachmentEntity?,
        numberAttachments: Int,
        selfDestructTime: Int,
        quote: String
    ) {
        viewModelScope.launch {
            val durationAttachment =
                TimeUnit.MILLISECONDS.toSeconds(attachmentEntity?.duration ?: 0).toInt()
            val selfAutoDestruction = compareDurationAttachmentWithSelfAutoDestructionInSeconds(
                durationAttachment, selfDestructTime
            )

            if (messageString.isNotEmpty() || attachmentEntity != null) {
                val message = MessageEntity(
                    id = 0,
                    webId = "",
                    uuid = UUID.randomUUID().toString(),
                    body = messageString,
                    quoted = quote,
                    contactId = contact.id,
                    updatedAt = 0,
                    createdAt = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toInt(),
                    isMine = Constants.IsMine.YES.value,
                    status = Constants.MessageStatus.SENDING.status,
                    numberAttachments = numberAttachments,
                    messageType = Constants.MessageType.MESSAGE.type,
                    selfDestructionAt = selfAutoDestruction
                )

//                if (BuildConfig.ENCRYPT_API) {
//                    message.encryptBody(cryptoMessage)
//                }

                val messageId = repository.insertMessage(message).toInt()
                Timber.d("insertMessage")
                _newMessageSend.value = true

                message.id = messageId

                deleteMessageNotSent(contact.id)

                attachmentEntity?.let {
                    attachmentEntity.messageId = messageId

                    val attachmentId = repository.insertAttachment(attachmentEntity)
                    attachmentEntity.id = attachmentId.toInt()
                }

                if (message.quoted.isNotEmpty()) {
                    repository.insertQuote(quote, message)
                }

                sendMessageAndAttachment(
                    attachmentEntity = attachmentEntity,
                    messageEntity = message,
                    numberAttachments = numberAttachments,
                    selfDestructTime = selfAutoDestruction,
                    quote = quote
                )
            }
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

                val attachment = AttachmentEntity(
                    id = 0,
                    messageId = 0,
                    webId = "",
                    messageWebId = "",
                    type = Constants.AttachmentType.AUDIO.type,
                    body = "",
                    fileName = audioFile.name,
                    origin = Constants.AttachmentOrigin.AUDIO_SELECTION.origin,
                    thumbnailUri = "",
                    status = Constants.AttachmentStatus.SENDING.status,
                    extension = "mp3",
                    duration = mediaStoreAudio.duration
                )

                saveMessageAndAttachment(
                    messageString = "",
                    attachmentEntity = attachment,
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
        attachmentEntity: AttachmentEntity?,
        messageEntity: MessageEntity,
        numberAttachments: Int,
        selfDestructTime: Int,
        quote: String = ""
    ) {
        try {
            val messageReqDTO = MessageReqDTO(
                userDestination = contact.id,
                quoted = quote,
                body = messageEntity.getBody(cryptoMessage),
                numberAttachments = numberAttachments,
                destroy = selfDestructTime,
                messageType = Constants.MessageType.MESSAGE.type,
                uuidSender = messageEntity.uuid
            )

            val messageResponse = repository.sendMessage(messageReqDTO)

            if (messageResponse.isSuccessful) {
                val messageEntity = MessageResDTO.toMessageEntity(
                    messageEntity,
                    messageResponse.body()!!,
                    Constants.IsMine.YES.value
                )

                if (attachmentEntity != null) {
                    attachmentEntity.messageWebId = messageResponse.body()!!.id
                    uploadAttachment(attachmentEntity, messageEntity, selfDestructTime)
                } else {
                    messageEntity.status =
                        if (messageEntity.isMine == Constants.IsMine.NO.value) Constants.MessageStatus.UNREAD.status
                        else Constants.MessageStatus.SENT.status
                    repository.updateMessage(messageEntity)
                    Timber.d("updateMessage")
                }

                //setupNotificationSound(context, R.raw.tone_send_message)

            } else {
                setStatusErrorMessageAndAttachment(messageEntity, attachmentEntity)

                when (messageResponse.code()) {
                    Constants.CodeHttp.UNPROCESSABLE_ENTITY.code -> _webServiceError.value =
                        repository.getUnprocessableEntityErrorMessage(messageResponse)
                    else -> _webServiceError.value = repository.getErrorMessage(messageResponse)
                }
            }
        } catch (e: Exception) {
            setStatusErrorMessageAndAttachment(messageEntity, attachmentEntity)
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

    override fun deleteMessagesSelected(contactId: Int, listMessageRelations: List<MessageAttachmentRelation>) {
        viewModelScope.launch {
            repository.deleteMessagesSelected(contactId, listMessageRelations)
            _responseDeleteLocalMessages.value = true
        }
    }

    override fun deleteMessagesForAll(contactId: Int, listMessageRelations: List<MessageAttachmentRelation>) {
        viewModelScope.launch {
            try {
                val response =
                    repository.deleteMessagesForAll(
                        buildObjectDeleteMessages(
                            contactId,
                            listMessageRelations.filter { messageAndAttachment ->
                                messageAndAttachment.messageEntity.webId.isNotEmpty()
                            }
                        )
                    )

                if (response.isSuccessful) {
                    repository.deleteMessagesSelected(contactId, listMessageRelations)
                    _responseDeleteLocalMessages.value = true
                } else {
                    when (response.code()) {
                        Constants.CodeHttp.UNPROCESSABLE_ENTITY.code -> {
                            _deleteMessagesForAllWsError.value =
                                repository.getUnprocessableEntityErrorDeleteMessagesForAll(response.errorBody()!!)
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

    override fun deleteMessageNotSent(contactId: Int){
        repository.deleteMessageNotSent(contactId)
    }

    private fun buildObjectDeleteMessages(
        contactId: Int,
        listMessageRelations: List<MessageAttachmentRelation>
    ): DeleteMessagesReqDTO {
        val listReturn = arrayListOf<String>()
        listMessageRelations.forEach {
            listReturn.add(it.messageEntity.webId)
        }
        return DeleteMessagesReqDTO(
            userReceiver = contactId,
            messagesId = listReturn
        )
    }

    override fun getMessagePosition(messageAndAttachmentRelation: MessageAttachmentRelation): Int {
        var index = -1

        messageAndAttachmentRelation.quoteEntity?.let { quote ->
            messageMessagesRelation.value?.let { messagesList ->
                index = messagesList.indexOfFirst { it.messageEntity.id == quote.messageParentId }
            }
        }

        return index
    }

    override fun callContact() {
        val channel = "presence-private.${contact.id}_${userEntity.id}"
        _contactCalledSuccessfully.value = channel
    }

    override fun resetContactCalledSuccessfully() {
        _contactCalledSuccessfully.value = null
    }

    override fun resetNoInternetConnection() {
        _noInternetConnection.value = null
    }

    override fun setIsVideoCall(isVideoCall: Boolean) {
        this.isVideoCall = isVideoCall
    }

    override fun isVideoCall() = this.isVideoCall

    override fun resetIsVideoCall() {
        this.isVideoCall = false
    }

    override fun uploadAttachment(
        attachmentEntity: AttachmentEntity,
        messageEntity: MessageEntity,
        selfDestructTime: Int
    ) {
        viewModelScope.launch {
            val durationAttachment = TimeUnit.MILLISECONDS.toSeconds(attachmentEntity.duration).toInt()
            val selfAutoDestruction = compareDurationAttachmentWithSelfAutoDestructionInSeconds(
                durationAttachment, selfDestructTime
            )
            messageEntity.selfDestructionAt = selfAutoDestruction
            try {
                if (messageEntity.status == Constants.MessageStatus.ERROR.status && messageEntity.webId.isEmpty()) {
                    val messageReqDTO = MessageReqDTO(
                        userDestination = contact.id,
                        quoted = messageEntity.quoted,
                        body = messageEntity.getBody(cryptoMessage),
                        numberAttachments = 1,
                        destroy = selfAutoDestruction,
                        messageType = Constants.MessageType.MESSAGE.type,
                        uuidSender = messageEntity.uuid
                    )

                    val messageResponse = repository.sendMessage(messageReqDTO)

                    if (messageResponse.isSuccessful) {
                        val messageEntity = MessageResDTO.toMessageEntity(
                            messageEntity,
                            messageResponse.body()!!,
                            Constants.IsMine.YES.value
                        )

                        attachmentEntity.messageWebId = messageResponse.body()!!.id
                        uploadAttachment(attachmentEntity, messageEntity, selfDestructTime)
                    } else {
                        setStatusErrorMessageAndAttachment(messageEntity, attachmentEntity)

                        when (messageResponse.code()) {
                            Constants.CodeHttp.UNPROCESSABLE_ENTITY.code -> _webServiceError.value =
                                repository.getUnprocessableEntityErrorMessage(messageResponse)
                            else -> _webServiceError.value =
                                repository.getErrorMessage(messageResponse)
                        }
                    }
                } else {
                    repository.suspendUpdateAttachment(attachmentEntity)
                    val intent = Intent(context, UploadService::class.java).apply {
                        putExtras(Bundle().apply {
                            putParcelable(UploadService.MESSAGE_KEY, messageEntity)
                            putParcelable(UploadService.ATTACHMENT_KEY, attachmentEntity)
                        })
                    }
                    context.startService(intent)
                    /*repository.uploadAttachment(attachment, message)
                        .flowOn(Dispatchers.IO)
                        .collect {
                            _uploadProgress.value = it
                        }*/
                }
            } catch (e: Exception) {
                setStatusErrorMessageAndAttachment(messageEntity, attachmentEntity)
                Timber.e(e)
            }
        }
    }

    override fun downloadAttachment(messageAndAttachmentRelation: MessageAttachmentRelation, itemPosition: Int) {
        viewModelScope.launch {
            repository.downloadAttachment(messageAndAttachmentRelation, itemPosition)
                .flowOn(Dispatchers.IO)
                .onStart {
                    messageAndAttachmentRelation.getFirstAttachment()?.let { attachment ->

                        val fileName = "${System.currentTimeMillis()}.${attachment.extension}"
                        attachment.status = Constants.AttachmentStatus.DOWNLOADING.status
                        attachment.fileName = fileName
                        Timber.d("Attachment status: ${attachment.status}, uri: ${attachment.fileName}")
                        updateAttachment(attachment)
                        _downloadProgress.value =
                            DownloadAttachmentResult.Start(itemPosition, this@launch as Job)
                    }
                }
                .catch {

                    Timber.e("catch flow")

                    val message = messageAndAttachmentRelation.messageEntity
                    val firstAttachment = messageAndAttachmentRelation.getFirstAttachment()

                    message.status = Constants.MessageStatus.ERROR.status
                    updateMessage(message)

                    if (firstAttachment != null) {
                        firstAttachment.status = Constants.AttachmentStatus.DOWNLOAD_CANCEL.status
                        updateAttachment(firstAttachment)
                    }

                    _downloadProgress.value =
                        DownloadAttachmentResult.Cancel(messageAndAttachmentRelation, itemPosition)
                }
                .collect {
                    _downloadProgress.value = it
                }
        }
    }

    override fun updateMessage(messageEntity: MessageEntity) {
        repository.updateMessage(messageEntity)
    }

    override fun updateAttachment(attachmentEntity: AttachmentEntity) {
        repository.updateAttachment(attachmentEntity)
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

    override fun sendMessageRead(messageAndAttachmentRelation: MessageAttachmentRelation) {
        viewModelScope.launch {
            repository.setMessageRead(messageAndAttachmentRelation)
        }
    }

    override fun sendMessageRead(messageWebId: String) {
        viewModelScope.launch {
            repository.setMessageRead(messageWebId)
        }
    }

    override fun reSendMessage(messageEntity: MessageEntity, selfDestructTime: Int) {
        viewModelScope.launch {
            try {

                val messageReqDTO = MessageReqDTO(
                    userDestination = contact.id,
                    quoted = messageEntity.quoted,
                    body = messageEntity.body,
                    numberAttachments = 0,
                    destroy = selfDestructTime,
                    messageType = Constants.MessageType.MESSAGE.type,
                    uuidSender = messageEntity.uuid
                )

                _stateMessage.value = StateMessage.Start(messageEntity.id)

                val messageResponse = repository.sendMessage(messageReqDTO)

                if (messageResponse.isSuccessful) {

                    _stateMessage.value = StateMessage.Success(messageEntity.id)

                    val messageEntity = MessageResDTO.toMessageEntity(
                        messageEntity,
                        messageResponse.body()!!,
                        Constants.IsMine.YES.value
                    )

                    messageEntity.status =
                        if (messageEntity.isMine == Constants.IsMine.NO.value) Constants.MessageStatus.UNREAD.status
                        else Constants.MessageStatus.SENT.status
                    repository.updateMessage(messageEntity)
                    Timber.d("updateMessage")
                } else {

                    _stateMessage.value = StateMessage.Error(messageEntity.id)

                    setStatusErrorMessageAndAttachment(messageEntity, null)

                    when (messageResponse.code()) {
                        Constants.CodeHttp.UNPROCESSABLE_ENTITY.code -> _webServiceError.value =
                            repository.getUnprocessableEntityErrorMessage(messageResponse)
                        else -> _webServiceError.value = repository.getErrorMessage(messageResponse)
                    }
                }
            } catch (e: Exception) {
                _stateMessage.value = StateMessage.Error(messageEntity.id)

                setStatusErrorMessageAndAttachment(messageEntity, null)
                Timber.e(e)
            }
        }
    }

    override fun resetNewMessage() {
        _newMessageSend.value = null
    }

    override fun getFreeTrial() = repository.getFreeTrial()

    override fun getMessageNotSent(contactId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _messageNotSent.postValue(repository.getMessageNotSent(contactId))
        }
    }

    override fun insertMessageNotSent(message: String, contactId: Int) {
        repository.insertMessageNotSent(message, contactId)
    }

    //endregion
}
