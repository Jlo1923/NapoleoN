package com.naposystems.pepito.ui.conversation

import android.content.Context
import androidx.lifecycle.*
import androidx.paging.PagedList
import com.naposystems.pepito.R
import com.naposystems.pepito.dto.conversation.message.ConversationAttachmentResDTO
import com.naposystems.pepito.dto.conversation.message.ConversationReqDTO
import com.naposystems.pepito.dto.conversation.message.ConversationResDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.conversation.Conversation
import com.naposystems.pepito.entity.conversation.ConversationAttachment
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.entity.conversation.ConversationAndAttachment
import com.naposystems.pepito.utility.Constants
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


class ConversationViewModel @Inject constructor(
    private val context: Context,
    private val repository: IContractConversation.Repository
) : ViewModel(), IContractConversation.ViewModel {

    private lateinit var user: User

    private val _webServiceError = MutableLiveData<List<String>>()
    val webServiceError: LiveData<List<String>>
        get() = _webServiceError

    private lateinit var _conversationMessages: LiveData<PagedList<ConversationAndAttachment>>
    val conversationMessages: LiveData<PagedList<ConversationAndAttachment>>
        get() = _conversationMessages

    private val _channelName = MutableLiveData<String>()
    val channelName: LiveData<String>
        get() = _channelName

    init {
        _webServiceError.value = ArrayList()

    }

    //region Implementation IContractConversation.ViewModel

    override fun getUser() = user

    override fun subscribeToChannel(userToChat: Contact) {
        viewModelScope.launch {
            _channelName.value = repository.subscribeToChannel(userToChat)
        }
    }

    override fun unSubscribeToChannel(userToChat: Contact) {
        repository.unSubscribeToChannel(userToChat, channelName.value!!)
    }

    override fun getLocalMessages() {
        viewModelScope.launch {
            user = repository.getLocalUser()
            _conversationMessages = repository.getLocalMessages(_channelName.value!!, 10)
        }
    }

    override fun getRemoteMessages(channelName: String, contactId: Int) {
        viewModelScope.launch {
            repository.getRemoteMessages(user, contactId, channelName)
        }
    }

    override fun saveConversationLocally(
        body: String,
        quoted: String,
        contact: Contact,
        isMine: Int
    ) {
        viewModelScope.launch {
            val conversation = Conversation(
                0,
                "",
                body,
                quoted,
                contact.id,
                user.id,
                "",
                "",
                isMine,
                _channelName.value!!
            )


            val conversationId = repository.insertConversation(conversation).toInt()

            val conversationReqDTO =
                ConversationReqDTO(
                    contact.id,
                    quoted,
                    body,
                    emptyList()
                )

            sendMessage(
                conversationId,
                conversationReqDTO,
                isMine,
                emptyList()
            )
        }
    }

    override fun saveConversationWithAttachmentLocally(
        body: String,
        quoted: String,
        contact: Contact,
        isMine: Int,
        base64: String,
        uri: String
    ) {
        viewModelScope.launch {
            val conversation = Conversation(
                0,
                "",
                body,
                quoted,
                contact.id,
                user.id,
                "",
                "",
                isMine,
                _channelName.value!!
            )


            val conversationId = repository.insertConversation(conversation).toInt()

            val attachment = ConversationAttachment(
                0,
                conversationId,
                "",
                "",
                Constants.ConversationAttachmentType.IMAGE.type,
                "",
                uri
            )
            val listAttachment: MutableList<ConversationAttachment> = ArrayList()
            listAttachment.add(attachment)

            val listAttachmentId = repository.insertConversationAttachment(listAttachment)

            attachment.body = base64

            val conversationReqDTO =
                ConversationReqDTO(
                    contact.id,
                    quoted,
                    body,
                    ConversationAttachment.toListConversationAttachmentDTO(listAttachment)
                )

            sendMessage(
                conversationId,
                conversationReqDTO,
                isMine,
                listAttachmentId
            )
        }
    }

    override fun sendMessage(
        conversationId: Int,
        conversationReqDTO: ConversationReqDTO,
        isMine: Int,
        listAttachmentsId: List<Long>
    ) {
        viewModelScope.launch {
            try {
                val response = repository.sendMessage(conversationReqDTO)

                if (response.isSuccessful) {
                    Timber.d("Message send successFully")
                    val conversationEntity = ConversationResDTO.toConversationEntity(
                        conversationId,
                        response.body()!!,
                        isMine,
                        _channelName.value!!
                    )
                    repository.updateConversation(conversationEntity)

                    if (listAttachmentsId.isNotEmpty()) {
                        repository.updateConversationAttachments(
                            ConversationAttachmentResDTO.toListConversationAttachment(
                                response.body()!!.conversationAttachments,
                                conversationId,
                                listAttachmentsId
                            )
                        )
                    }
                } else {
                    when (response.code()) {
                        422 -> _webServiceError.value = repository.get422Error(response)
                        else -> _webServiceError.value = repository.getError(response)
                    }
                }
            } catch (e: Exception) {
                Timber.d(e)
                val error = context.getString(R.string.something_went_wrong)
                _webServiceError.value = arrayListOf(error)
            }
        }
    }

    //endregion
}
