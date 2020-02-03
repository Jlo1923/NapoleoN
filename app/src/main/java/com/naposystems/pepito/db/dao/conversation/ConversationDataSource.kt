package com.naposystems.pepito.db.dao.conversation

import androidx.lifecycle.LiveData
import com.naposystems.pepito.dto.conversation.message.MessageResDTO
import com.naposystems.pepito.entity.conversation.ConversationAndContact

interface ConversationDataSource {

    suspend fun insertConversations(messagesResList: List<MessageResDTO>, isMine: Boolean)

    suspend fun insertConversation(
        messageRes: MessageResDTO,
        isMine: Boolean,
        unreadMessages: Int
    )

    fun getConversations(): LiveData<List<ConversationAndContact>>

    suspend fun updateConversation(contactId: Int)
}