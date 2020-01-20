package com.naposystems.pepito.db.dao.conversation

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.naposystems.pepito.entity.conversation.Conversation
import com.naposystems.pepito.entity.conversation.ConversationAndAttachment

interface ConversationDataSource {

    fun getMessages(channelName: String, pageSize: Int): LiveData<PagedList<ConversationAndAttachment>>

    fun insertConversation(conversation: Conversation): Long

    fun insertListConversation(conversationList: List<Conversation>)

    fun updateConversation(conversation: Conversation)
}