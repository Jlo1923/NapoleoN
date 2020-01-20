package com.naposystems.pepito.db.dao.conversation

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.naposystems.pepito.entity.conversation.Conversation
import com.naposystems.pepito.entity.conversation.ConversationAndAttachment

@Dao
interface ConversationDao {

    @Query("SELECT * FROM conversation WHERE channel_name=:channelName ORDER BY id DESC")
    fun getMessagesAndAttachments(channelName: String): DataSource.Factory<Int, ConversationAndAttachment>

    @Insert
    fun insertConversation(conversation: Conversation): Long

    @Insert
    fun insertConversationList(conversationList: List<Conversation>)

    @Update
    fun updateConversation(conversation: Conversation)
}