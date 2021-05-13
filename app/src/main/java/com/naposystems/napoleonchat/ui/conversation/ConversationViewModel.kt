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
import com.naposystems.napoleonchat.model.MediaStoreAudio
import com.naposystems.napoleonchat.service.download.model.DownloadAttachmentResult
import com.naposystems.napoleonchat.service.multiattachment.MultipleUploadService
import com.naposystems.napoleonchat.service.uploadService.UploadService
import com.naposystems.napoleonchat.source.local.entity.*
import com.naposystems.napoleonchat.source.remote.dto.conversation.deleteMessages.DeleteMessagesReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageResDTO
import com.naposystems.napoleonchat.ui.conversation.model.ItemMessage
import com.naposystems.napoleonchat.ui.conversation.model.ItemMessageWithMsgEntity
import com.naposystems.napoleonchat.ui.conversation.model.toItemMessageWithMsgEntity
import com.naposystems.napoleonchat.ui.multipreview.contract.IContractMultipleAttachmentPreview
import com.naposystems.napoleonchat.ui.selfDestructTime.IContractSelfDestructTime
import com.naposystems.napoleonchat.utility.*
import com.naposystems.napoleonchat.utility.Utils.Companion.compareDurationAttachmentWithSelfAutoDestructionInSeconds
import com.naposystems.napoleonchat.utility.extensions.getMessageEntityForCreate
import com.naposystems.napoleonchat.utility.extensions.getSelfAutoDestructionForSave
import com.naposystems.napoleonchat.utility.extensions.toAttachmentEntityAudio
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import retrofit2.Response
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
    private val repository: IContractConversation.Repository,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val repositoryMessages: IContractMultipleAttachmentPreview.Repository,
    private val repositorySelfDestruction: IContractSelfDestructTime.Repository
) : ViewModel(), IContractConversation.ViewModel {

    private lateinit var userEntity: UserEntity
    private lateinit var contactEntity: ContactEntity
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

    private fun setStatusErrorMessageAndAttachment(
        messageEntity: MessageEntity,
        attachmentEntity: AttachmentEntity?
    ) {
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
        this.contactEntity = contact
    }

    override fun getLocalMessages() {
        viewModelScope.launch {
            userEntity = repository.getLocalUser()
//            repository.verifyMessagesToDelete()
            _messageMessagesRelation = repository.getLocalMessages(contactEntity.id)
        }
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    override fun saveMessageLocally(body: String, selfDestructTime: Int, quote: String) {
        val itemMessage = ItemMessage(
            body,
            selfDestructTime = selfDestructTime,
            quote = quote,
            contact = contactEntity
        )
        saveMessageAndAttachment(itemMessage)
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    override fun saveMessageAndAttachment(
        itemMessage: ItemMessage
    ) {
        viewModelScope.launch {
            itemMessage.apply {
                this.contact = contactEntity // TODO: this can change
                val selfAutoDestruction =
                    attachment?.getSelfAutoDestructionForSave(selfDestructTime) ?: 0

                if (messageString.isNotEmpty() || attachment != null) {
                    attachment?.selfDestructionAt = selfAutoDestruction
                    val message = insertNewMessage(selfAutoDestruction)
                    contact?.let { deleteMessageNotSent(it.id) }
                    tryInsertAttachIfExists(message, attachment)
                    if (message.quoted.isNotEmpty()) repository.insertQuote(quote, message)
                    sendMessageAndAttachment(this.toItemMessageWithMsgEntity(message))
                }
            }
        }
    }

    private fun tryInsertAttachIfExists(
        message: MessageEntity,
        attachment: AttachmentEntity?
    ) {
        attachment?.let {
            it.messageId = message.id
            it.id = 0
            val attachmentId = repository.insertAttachment(attachment)
            attachment.id = attachmentId.toInt()
        }
    }

    private suspend fun ItemMessage.insertNewMessage(selfAutoDestruction: Int): MessageEntity {
        val message = this.getMessageEntityForCreate()
        message.selfDestructionAt = selfAutoDestruction
        // if (BuildConfig.ENCRYPT_API) message.encryptBody(cryptoMessage)
        message.id = repository.insertMessage(message).toInt()
        Timber.d("insertMessage")
        _newMessageSend.value = true
        return message
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
                val attachment = audioFile.toAttachmentEntityAudio(mediaStoreAudio)
                saveMessageAndAttachment(
                    ItemMessage(
                        attachment = attachment,
                        numberAttachments = 1,
                        selfDestructTime = selfDestructTime,
                        quote = quote,
                        contact = contactEntity
                    )
                )
            }
        }
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    private suspend fun sendMessageAndAttachment(
        itemMessage: ItemMessageWithMsgEntity
    ) {
        try {
            itemMessage.apply {

                val messageReqDTO = this.toMessageReqDto(cryptoMessage)
                val messageResponse = repository.sendMessage(messageReqDTO)

                if (messageResponse.isSuccessful) {

                    val messageEntityFromResDto = MessageResDTO.toMessageEntity(
                        messageEntity,
                        messageResponse.body()!!,
                        Constants.IsMine.YES.value
                    )

                    if (attachment != null) {
                        attachment.messageWebId = messageResponse.body()!!.id
                        uploadAttachment(attachment, messageEntityFromResDto, selfDestructTime)
                    } else {
                        messageEntityFromResDto.status =
                            if (isTheMsgMine(messageEntityFromResDto)) Constants.MessageStatus.UNREAD.status
                            else Constants.MessageStatus.SENT.status
                        repository.updateMessage(messageEntityFromResDto)

                    }

                    //setupNotificationSound(context, R.raw.tone_send_message)

                } else {
                    setStatusErrorMessageAndAttachment(messageEntity, attachment)
                    handleMessageResponseCode(messageResponse)
                }
            }
        } catch (e: Exception) {
            itemMessage.apply {
                setStatusErrorMessageAndAttachment(messageEntity, attachment)
                Timber.e(e)
            }
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

    override fun deleteMessagesSelected(
        contactId: Int,
        listMessageRelations: List<MessageAttachmentRelation>
    ) {
        viewModelScope.launch {
            repository.deleteMessagesSelected(contactId, listMessageRelations)
            _responseDeleteLocalMessages.value = true
        }
    }

    override fun deleteMessagesForAll(
        contactId: Int,
        listMessageRelations: List<MessageAttachmentRelation>
    ) {
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
            repository.sendTextMessagesRead(contactEntity.id)
            repository.sendMissedCallRead(contactEntity.id)
        }
    }

    override fun deleteMessageNotSent(contactId: Int) {
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
        listMessageRelations
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
        val mayor: Int
        val minor: Int

        if (contactEntity.id > userEntity.id) {
            mayor = contactEntity.id
            minor = userEntity.id
        } else {
            mayor = userEntity.id
            minor = contactEntity.id
        }

        val channel = "presence-private.${minor}_${mayor}"
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
            val durationAttachment =
                TimeUnit.MILLISECONDS.toSeconds(attachmentEntity.duration).toInt()
            val selfAutoDestruction = compareDurationAttachmentWithSelfAutoDestructionInSeconds(
                durationAttachment, selfDestructTime
            )
            messageEntity.selfDestructionAt = selfAutoDestruction
            try {
                if (messageEntity.status == Constants.MessageStatus.ERROR.status && messageEntity.webId.isEmpty()) {
                    val messageReqDTO = MessageReqDTO(
                        userDestination = contactEntity.id,
                        quoted = messageEntity.quoted,
                        body = messageEntity.getBody(cryptoMessage),
                        numberAttachments = 1,
                        destroy = selfAutoDestruction,
                        messageType = Constants.MessageTextType.NORMAL.type,
                        uuidSender = messageEntity.uuid ?: UUID.randomUUID().toString()
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
                        handleMessageResponseCode(messageResponse)
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

    override fun downloadAttachment(
        messageAndAttachmentRelation: MessageAttachmentRelation,
        itemPosition: Int
    ) {
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

    override fun sendMessageRead(messageId: Int, webId: String) {
        GlobalScope.launch {
            repository.setMessageRead(messageId, webId)
        }
    }

    override fun reSendMessage(messageEntity: MessageEntity, selfDestructTime: Int) {
        viewModelScope.launch {
            try {

                val messageReqDTO = MessageReqDTO(
                    userDestination = contactEntity.id,
                    quoted = messageEntity.quoted,
                    body = messageEntity.body,
                    numberAttachments = 0,
                    destroy = selfDestructTime,
                    messageType = Constants.MessageTextType.NORMAL.type,
                    uuidSender = messageEntity.uuid ?: UUID.randomUUID().toString()
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
                        if (isTheMsgMine(messageEntity)) Constants.MessageStatus.UNREAD.status
                        else Constants.MessageStatus.SENT.status
                    repository.updateMessage(messageEntity)
                    Timber.d("updateMessage")
                } else {

                    _stateMessage.value = StateMessage.Error(messageEntity.id)

                    setStatusErrorMessageAndAttachment(messageEntity, null)

                    handleMessageResponseCode(messageResponse)
                }
            } catch (e: Exception) {
                _stateMessage.value = StateMessage.Error(messageEntity.id)

                setStatusErrorMessageAndAttachment(messageEntity, null)
                Timber.e(e)
            }
        }
    }

    private fun handleMessageResponseCode(messageResponse: Response<MessageResDTO>) {
        when (messageResponse.code()) {
            Constants.CodeHttp.UNPROCESSABLE_ENTITY.code -> _webServiceError.value =
                repository.getUnprocessableEntityErrorMessage(messageResponse)
            else -> _webServiceError.value = repository.getErrorMessage(messageResponse)
        }
    }

    private fun isTheMsgMine(messageEntityFromResDto: MessageEntity) =
        messageEntityFromResDto.isMine == Constants.IsMine.NO.value

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

    fun getPendingUris(): List<Uri> {
        val urisString = sharedPreferencesManager.getStringSet("test")
        val listString = urisString?.toList()
        val listUris = listString?.map { Uri.parse(it) }
        return listUris ?: emptyList()
    }

    fun removePendingUris() {
        sharedPreferencesManager.puStringSet("test", emptyList())
    }

    fun sendMessageToRemote(messageEntity: MessageEntity, attachments: List<AttachmentEntity?>) {
        if (messageEntity.mustSendToRemote()) {
            viewModelScope.launch {
                try{
                    val messageResponse = repositoryMessages.sendMessage(messageEntity)
                    val attachmentsWithWebId =
                        setMessageWebIdToAttachments(attachments, messageResponse)
                    repositorySelfDestruction.updateAttachments(attachmentsWithWebId)
                    messageResponse?.let { pairData ->
                        pairData.first?.let { sendMessageToRemote(it, attachmentsWithWebId) }
                    }
                }catch (exception: Exception){
                    messageEntity.status = Constants.MessageStatus.ERROR.status
                    repository.updateMessage(messageEntity, false)
                }
            }
        } else {
            initUploadServiceForSendFiles(messageEntity, attachments)
        }
    }

    private fun initUploadServiceForSendFiles(
        messageEntity: MessageEntity,
        attachments: List<AttachmentEntity?>
    ) {
        // we can create notification for upload attachments
        // todo: mover esto a un activity para usar el context
        val intent = Intent(context, MultipleUploadService::class.java).apply {
            putExtras(Bundle().apply {
                putParcelable(MultipleUploadService.MESSAGE_KEY, messageEntity)
                putParcelableArrayList(MultipleUploadService.ATTACHMENT_KEY, ArrayList(attachments))
            })
        }
        context.startService(intent)
    }

    private fun setMessageWebIdToAttachments(
        attachments: List<AttachmentEntity?>,
        messageResponse: Pair<MessageEntity?, String>?
    ): List<AttachmentEntity?> {
        attachments.forEach { attachment ->
            attachment?.let {
                it.messageWebId = messageResponse?.second ?: ""
            }
        }
        return attachments
    }

    //endregion
}


