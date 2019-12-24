package com.naposystems.pepito.ui.conversation

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.naposystems.pepito.R
import com.naposystems.pepito.dto.conversation.message.ConversationReqDTO
import com.naposystems.pepito.dto.conversation.message.ConversationResDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.Conversation
import com.naposystems.pepito.entity.User
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

    private lateinit var _conversationMessages: LiveData<PagedList<Conversation>>
    val conversationMessages: LiveData<PagedList<Conversation>>
        get() = _conversationMessages

    private val _channelName = MutableLiveData<String>()
    val channelName: LiveData<String>
        get() = _channelName

    init {
        _webServiceError.value = ArrayList()

    }

    //region Implementation IContractConversation.ViewModel

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
            val user = repository.getLocalUser()
            repository.getRemoteMessages(user, contactId, channelName)
        }
    }

    override fun saveConversationLocally(
        body: String,
        type: String,
        contact: Contact,
        isMine: Int
    ) {
        viewModelScope.launch {
            user = repository.getLocalUser()

            val conversation = Conversation(
                0,
                "",
                body,
                type,
                contact.id,
                user.id,
                "",
                "",
                isMine,
                _channelName.value!!
            )

            val conversationId = repository.insertConversation(conversation)

            val conversationReqDTO =
                ConversationReqDTO(
                    contact.id,
                    type,
                    body
                )

            sendMessage(conversationId.toInt(), conversationReqDTO, isMine)
        }
    }

    override fun sendMessage(
        conversationId: Int,
        conversationReqDTO: ConversationReqDTO,
        isMine: Int
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
