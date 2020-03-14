package com.naposystems.pepito.ui.conversation

import android.content.Context
import android.os.ParcelFileDescriptor
import androidx.lifecycle.*
import androidx.paging.PagedList
import com.naposystems.pepito.R
import com.naposystems.pepito.dto.conversation.message.MessageReqDTO
import com.naposystems.pepito.dto.conversation.message.MessageResDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.entity.message.attachments.MediaStoreAudio
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject
import androidx.lifecycle.LiveData
import com.naposystems.pepito.dto.conversation.deleteMessages.DeleteMessagesReqDTO
import com.naposystems.pepito.utility.FileManager
import java.util.concurrent.TimeUnit

class ConversationViewModel @Inject constructor(
    private val context: Context,
    private val repository: IContractConversation.Repository
) : ViewModel(), IContractConversation.ViewModel {

    private lateinit var user: User
    private lateinit var contact: Contact
    lateinit var contactProfile: LiveData<Contact>

    private val _webServiceError = MutableLiveData<List<String>>()
    val webServiceError: LiveData<List<String>>
        get() = _webServiceError

    private lateinit var _messageMessages: LiveData<PagedList<MessageAndAttachment>>
    val messageMessages: LiveData<PagedList<MessageAndAttachment>>
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

    private var countOldMessages: Int = 0

    init {
        _responseDeleteLocalMessages.value = false
        _webServiceError.value = ArrayList()
        _stringsCopy.value = emptyList()
    }

    private suspend fun copyAudioToAppFolder(fileDescriptor: ParcelFileDescriptor): File {

        val fileInputStream = FileInputStream(fileDescriptor.fileDescriptor)

        /*return Utils.copyEncryptedFile(
            context,
            fileInputStream,
            subFolder,
            "${System.currentTimeMillis()}.${mediaStoreAudio.extension}"
        )*/

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

    override fun getLocalContact(idContact: Int) {
        contactProfile = repository.getLocalContact(idContact)
    }

    override fun getLocalMessages() {
        viewModelScope.launch {
            user = repository.getLocalUser()
            _messageMessages = repository.getLocalMessages(contact.id, 10)
        }
    }

    override fun saveMessageLocally(body: String, selfDestructTime: Int) {
        saveMessageAndAttachment(body, null, 0, selfDestructTime)
    }

    override fun saveMessageAndAttachment(
        messageString: String,
        attachment: Attachment?,
        numberAttachments: Int,
        selfDestructTime: Int
    ) {
        viewModelScope.launch {
            val message = Message(
                id = 0,
                webId = "",
                body = messageString,
                quoted = "",
                contactId = contact.id,
                updatedAt = 0,
                createdAt = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toInt(),
                isMine = Constants.IsMine.YES.value,
                status = Constants.MessageStatus.SENDING.status,
                numberAttachments = numberAttachments
            )

            val messageId = repository.insertMessage(message).toInt()

            message.id = messageId

            attachment?.let {
                attachment.messageId = messageId

                val attachmentId = repository.insertAttachment(attachment)
                attachment.id = attachmentId.toInt()
            }

            sendMessageAndAttachment(
                attachment = attachment,
                message = message,
                numberAttachments = numberAttachments,
                selfDestructTime = selfDestructTime
            )
        }
    }

    override fun saveMessageWithAudioAttachment(
        mediaStoreAudio: MediaStoreAudio,
        selfDestructTime: Int
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
                    extension = "mp3"
                )

                saveMessageAndAttachment(
                    messageString = "",
                    attachment = attachment,
                    numberAttachments = 1,
                    selfDestructTime = selfDestructTime
                )
            }
        }
    }

    private suspend fun sendMessageAndAttachment(
        attachment: Attachment?,
        message: Message,
        numberAttachments: Int,
        selfDestructTime: Int
    ) {
        try {

            val messageReqDTO = MessageReqDTO(
                userDestination = contact.id,
                quoted = "",
                body = message.body,
                numberAttachments = numberAttachments,
                destroy = selfDestructTime
            )

            val messageResponse = repository.sendMessage(messageReqDTO)

            if (messageResponse.isSuccessful) {

                repository.insertConversation(messageResponse.body()!!)

                if (attachment != null) {

                    withContext(Dispatchers.IO) {

                        attachment.messageWebId = messageResponse.body()!!.id

                        try {
                            val responseAttachment =
                                repository.sendMessageAttachment(attachment)

                            if (responseAttachment.isSuccessful) {

                                responseAttachment.body()?.let { attachmentResDTO ->
                                    attachment.apply {
                                        webId = attachmentResDTO.id
                                        messageWebId = attachmentResDTO.messageId
                                        body = attachmentResDTO.body
                                        status = Constants.AttachmentStatus.SENT.status
                                    }
                                }

                                val messageEntity = MessageResDTO.toMessageEntity(
                                    message,
                                    messageResponse.body()!!,
                                    Constants.IsMine.YES.value
                                )
                                repository.updateMessage(messageEntity)

                                repository.updateAttachment(attachment)

                            } else {
                                setStatusErrorMessageAndAttachment(message, attachment)
                            }
                        } catch (e: Exception) {
                            setStatusErrorMessageAndAttachment(message, attachment)
                            Timber.e(e)
                        }
                    }
                } else {
                    val messageEntity = MessageResDTO.toMessageEntity(
                        message,
                        messageResponse.body()!!,
                        Constants.IsMine.YES.value
                    )
                    repository.updateMessage(messageEntity)
                }
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

    override fun updateStateSelectionMessage(idContact: Int, idMessage: Int, isSelected: Boolean) {
        viewModelScope.launch {
            repository.updateStateSelectionMessage(
                idContact,
                idMessage,
                Utils.convertBooleanToInvertedInt(isSelected)
            )
        }
    }

    override fun cleanSelectionMessages(idContact: Int) {
        viewModelScope.launch {
            repository.cleanSelectionMessages(idContact)
        }
    }

    override fun deleteMessagesSelected(idContact: Int, listMessages: List<MessageAndAttachment>) {
        viewModelScope.launch {
            repository.deleteMessagesSelected(idContact, listMessages)
            _responseDeleteLocalMessages.value = true
        }
    }

    override fun deleteMessagesForAll(idContact: Int, listMessages: List<MessageAndAttachment>) {
        viewModelScope.launch {
            try {

                val response =
                    repository.deleteMessagesForAll(
                        buildObjectDeleteMessages(
                            idContact,
                            listMessages
                        )
                    )

                if (response.isSuccessful) {
                    repository.deleteMessagesSelected(idContact, listMessages)
                    _responseDeleteLocalMessages.value = true
                } else {
                    when (response.code()) {
                        422 -> {
                            _deleteMessagesForAllWsError.value =
                                repository.get422ErrorDeleteMessagesForAll(response.errorBody()!!)
                        }
                        else -> {
                            _deleteMessagesForAllWsError.value =
                                repository.get422ErrorDeleteMessagesForAll(response.errorBody()!!)
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

    override fun copyMessagesSelected(idContact: Int) {
        viewModelScope.launch {
            _stringsCopy.value = repository.copyMessagesSelected(idContact)
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

    override fun getMessagesSelected(idContact: Int) {
        viewModelScope.launch {
            _messagesSelected = repository.getMessagesSelected(idContact)
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

    override fun sendMessagesRead() {
        viewModelScope.launch {
            repository.sendMessagesRead(contact.id)
        }
    }

    private fun buildObjectDeleteMessages(
        idContact: Int,
        listMessages: List<MessageAndAttachment>
    ): DeleteMessagesReqDTO {
        val listReturn = arrayListOf<String>()
        listMessages.forEach {
            listReturn.add(it.message.webId)
        }
        return DeleteMessagesReqDTO(
            userReceiver = idContact,
            messagesId = listReturn
        )
    }

    //endregion
}
