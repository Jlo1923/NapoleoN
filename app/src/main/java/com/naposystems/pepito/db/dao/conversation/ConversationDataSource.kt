package com.naposystems.pepito.db.dao.conversation

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.naposystems.pepito.entity.Conversation

interface ConversationDataSource {

    fun getMessages(channelName: String, pageSize: Int): LiveData<PagedList<Conversation>>

    fun insertConversation(conversation: Conversation): Long

    fun insertListConversation(conversationList: List<Conversation>)

    fun updateConversation(conversation: Conversation)
}