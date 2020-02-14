package com.naposystems.pepito.ui.conversation

import android.content.Context
import androidx.lifecycle.*
import androidx.paging.PagedList
import com.naposystems.pepito.R
import com.naposystems.pepito.dto.conversation.message.AttachmentResDTO
import com.naposystems.pepito.dto.conversation.message.MessageReqDTO
import com.naposystems.pepito.dto.conversation.message.MessageResDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.Attachment
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.utility.Constants
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


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

    init {
        _webServiceError.value = ArrayList()

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
        uri: String
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

            val attachment = Attachment(
                0,
                messageId,
                "",
                "",
                Constants.AttachmentType.IMAGE.type,
                "",
                uri
            )
            val listAttachment: MutableList<Attachment> = ArrayList()
            listAttachment.add(attachment)

            val listAttachmentId = repository.insertAttachment(listAttachment)

            attachment.body = base64

            val messageReqDTO =
                MessageReqDTO(
                    contact.id,
                    quoted,
                    body,
                    Attachment.toListAttachmentDTO(listAttachment)
                )

            sendMessage(
                messageId,
                messageReqDTO,
                isMine,
                listAttachmentId
            )
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
                            AttachmentResDTO.toListConversationAttachment(
                                response.body()!!.attachments,
                                messageId,
                                listAttachmentsId
                            )
                        )
                    }

                    repository.insertConversation(response.body()!!)
                } else {
                    when (response.code()) {
                        422 -> _webServiceError.value = repository.get422Error(response)
                        else -> _webServiceError.value = repository.getError(response)
                    }
                }
            } catch (e: Exception) {
                Timber.d(e)
                val error = context.getString(R.string.text_fail)
                _webServiceError.value = arrayListOf(error)
            }
        }
    }

    override fun sendMessagesRead() {
        viewModelScope.launch {
            repository.sendMessagesRead(contact.id)
        }
    }

    //endregion
}
