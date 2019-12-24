package com.naposystems.pepito.db.dao.conversation

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.naposystems.pepito.entity.Conversation
import javax.inject.Inject

class ConversationLocalDataSource @Inject constructor(
    private val conversationDao: ConversationDao
) : ConversationDataSource {

    override fun getMessages(
        channelName: String,
        pageSize: Int
    ): LiveData<PagedList<Conversation>> {

        val pagedListConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setInitialLoadSizeHint(10)
            .setPageSize(10 * 2)
            .build()

        val dataSourceFactory = conversationDao.getMessages(channelName)

        return LivePagedListBuilder(
            dataSourceFactory, pagedListConfig
        ).build()
    }

    override fun insertConversation(conversation: Conversation): Long {
        return conversationDao.insertConversation(conversation)
    }

    override fun insertListConversation(conversationList: List<Conversation>) {
        conversationDao.insertConversationList(conversationList)
    }

    override fun updateConversation(conversation: Conversation) {
        conversationDao.updateConversation(conversation)
    }
}