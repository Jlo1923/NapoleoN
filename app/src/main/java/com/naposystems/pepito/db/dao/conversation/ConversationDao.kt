package com.naposystems.pepito.db.dao.conversation

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.naposystems.pepito.entity.Conversation

@Dao
interface ConversationDao {

    @Query("SELECT * FROM conversation WHERE channel_name=:channel ORDER BY id DESC")
    fun getMessages(channel: String): DataSource.Factory<Int, Conversation>

    @Insert
    fun insertConversation(conversation: Conversation): Long

    @Insert
    fun insertConversationList(conversationList: List<Conversation>)

    @Update
    fun updateConversation(conversation: Conversation)
}