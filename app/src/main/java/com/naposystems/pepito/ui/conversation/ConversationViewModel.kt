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

    private fun copyAudioToAppFolder(
        fileDescriptor: ParcelFileDescriptor,
        mediaStoreAudio: MediaStoreAudio
    ): File {

        val fileInputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val subFolder = "conversations/${user.id}_${contact.id}"

        return Utils.copyEncryptedFile(
            context,
            fileInputStream,
            subFolder,
            "${System.currentTimeMillis()}.${mediaStoreAudio.extension}"
        )
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

    override fun saveMessageLocally(
        body: String,
        quoted: String,
        contact: Contact,
        isMine: Int
    ) {
        viewModelScope.launch {
            val message = Message(
                0,
                "",
                body,
                quoted,
                contact.id,
                0,
                0,
                isMine,
                Constants.MessageStatus.SENT.status
            )

            val messageId = repository.insertMessage(message).toInt()

            val messageReqDTO =
                MessageReqDTO(
                    contact.id,
                    quoted,
                    body,
                    emptyList()
                )

            sendMessage(
                messageId,
                messageReqDTO,
                isMine,
                emptyList()
            )
        }
    }

    override fun saveMessageWithAttachmentLocally(
        body: String,
        quoted: String,
        contact: Contact,
        isMine: Int,
        base64: String,
        uri: String,
        thumbnailUri: String,
        origin: Int,
        attachmentType: String
    ) {
        viewModelScope.launch {
            val message = Message(
                0,
                "",
                body,
                quoted,
                contact.id,
                0,
                0,
                isMine,
                Constants.MessageStatus.SENT.status
            )

            val messageId = repository.insertMessage(message).toInt()

            val attachment =
                Attachment(
                    id = 0,
                    messageId = messageId,
                    webId = "",
                    messageWebId = "",
                    type = attachmentType,
                    body = "",
                    uri = uri,
                    origin = origin,
                    thumbnailUri = thumbnailUri
                )
            val listAttachment: MutableList<Attachment> = ArrayList()
            listAttachment.add(attachment)

            val listAttachmentId = repository.insertAttachments(listAttachment)

            attachment.body = base64

            val messageReqDTO =
                MessageReqDTO(
                    contact.id,
                    quoted,
                    body,
                    Attachment.toListAttachmentDTO(listAttachment)
                )

            try {
                val response = repository.sendMessageTest(
                    userDestination = contact.id,
                    quoted = quoted,
                    body = body,
                    attachmentType = attachmentType,
                    uriString = uri
                )

                if (response.isSuccessful) {
                    Timber.d("Message send successFully")
                    val messageEntity = MessageResDTO.toMessageEntity(
                        messageId,
                        response.body()!!,
                        isMine
                    )
                    repository.updateMessage(messageEntity)

                    if (listAttachmentId.isNotEmpty()) {
                        repository.updateAttachments(
                            listAttachmentId,
                            response.body()!!.attachments
                        )
                    }

                    repository.insertConversation(response.body()!!)
                } else {
                    when (response.code()) {
                        422 -> _webServiceError.value = repository.get422ErrorMessage(response)
                        else -> _webServiceError.value = repository.getErrorMessage(response)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
            }

            /*sendMessage(
                messageId,
                messageReqDTO,
                isMine,
                listAttachmentId
            )*/
        }
    }

    override fun saveMessageWithAudioAttachment(mediaStoreAudio: MediaStoreAudio) {
        viewModelScope.launch {
            val fileDescriptor = context.contentResolver
                .openFileDescriptor(mediaStoreAudio.contentUri, "r")

            if (fileDescriptor != null) {

                val message = Message(
                    0,
                    "",
                    "",
                    "",
                    contact.id,
                    0,
                    0,
                    Constants.IsMine.YES.value,
                    Constants.MessageStatus.SENT.status
                )

                val messageId = repository.insertMessage(message).toInt()

                var audioFile: File? = null
                withContext(Dispatchers.IO) {
                    audioFile = copyAudioToAppFolder(fileDescriptor, mediaStoreAudio)
                }

                val attachment =
                    Attachment(
                        0,
                        messageId,
                        "",
                        "",
                        Constants.AttachmentType.AUDIO.type,
                        "",
                        audioFile!!.absolutePath,
                        Constants.AttachmentOrigin.AUDIO_SELECTION.origin
                    )

                val attachmentId = repository.insertAttachment(attachment)

                withContext(Dispatchers.IO) {
                    val encryptedFile = Utils.getEncryptedFile(context, audioFile!!)
                    attachment.body = Utils.convertFileInputStreamToBase64(
                        encryptedFile.openFileInput()
                    )
                }

                val messageReqDTO =
                    MessageReqDTO(
                        contact.id,
                        "",
                        "",
                        listOf(Attachment.toAttachmentDTO(attachment))
                    )

                sendMessage(
                    messageId,
                    messageReqDTO,
                    Constants.IsMine.YES.value,
                    listOf(attachmentId)
                )
            }
        }
    }

    override fun sendMessage(
        messageId: Int,
        messageReqDTO: MessageReqDTO,
        isMine: Int,
        listAttachmentsId: List<Long>
    ) {
        viewModelScope.launch {
            try {
                val response = repository.sendMessage(messageReqDTO)

                if (response.isSuccessful) {
                    Timber.d("Message send successFully")
                    val messageEntity = MessageResDTO.toMessageEntity(
                        messageId,
                        response.body()!!,
                        isMine
                    )
                    repository.updateMessage(messageEntity)

                    if (listAttachmentsId.isNotEmpty()) {
                        repository.updateAttachments(
                            listAttachmentsId,
                            response.body()!!.attachments
                        )
                    }

                    repository.insertConversation(response.body()!!)
                } else {
                    when (response.code()) {
                        422 -> _webServiceError.value = repository.get422ErrorMessage(response)
                        else -> _webServiceError.value = repository.getErrorMessage(response)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
                val error = context.getString(R.string.text_fail)
                _webServiceError.value = arrayListOf(error)
            }
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

    override fun deleteMessagesSelected(idContact: Int) {
        viewModelScope.launch {
            repository.deleteMessagesSelected(idContact)
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
                    repository.deleteMessagesSelected(idContact)
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
